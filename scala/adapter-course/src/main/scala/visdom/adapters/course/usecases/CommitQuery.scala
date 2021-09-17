package visdom.adapters.course.usecases

import com.mongodb.spark.MongoSpark
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.column
import org.apache.spark.sql.functions.udf
import spray.json.JsArray
import spray.json.JsObject
import spray.json.JsString
import visdom.adapters.course.AdapterValues.aPlusDatabaseName
import visdom.adapters.course.AdapterValues.gitlabDatabaseName
import visdom.adapters.course.schemas.CommitIdListSchema
import visdom.adapters.course.options.CommitQueryOptions
import visdom.adapters.course.output.CommitOutput
import visdom.adapters.course.output.ExerciseCommitsOutput
import visdom.adapters.course.output.FullCourseOutput
import visdom.adapters.course.output.ModuleCommitsOutput
import visdom.adapters.course.output.StudentCourseOutput
import visdom.adapters.course.schemas.CommitSchema
import visdom.adapters.course.schemas.CourseLinksSchema
import visdom.adapters.course.schemas.CourseSchema
import visdom.adapters.course.schemas.ExercisePointsSchema
import visdom.adapters.course.schemas.ExerciseSchema
import visdom.adapters.course.schemas.FileSchema
import visdom.adapters.course.schemas.GitSubmissionSchema
import visdom.adapters.course.schemas.MetadataOtherSchema
import visdom.adapters.course.schemas.ModuleLinksSchema
import visdom.adapters.course.schemas.ModuleSchema
import visdom.adapters.course.schemas.PointSchema
import visdom.adapters.course.schemas.SubmissionSchema
import visdom.database.mongodb.MongoConstants
import visdom.spark.ConfigUtils
import visdom.spark.Session
import visdom.utils.CommonConstants
import visdom.utils.SnakeCaseConstants


class CommitQuery(queryOptions: CommitQueryOptions) {
    val sparkSession: SparkSession = Session.getSparkSession()
    import sparkSession.implicits.newIntEncoder
    import sparkSession.implicits.newProductEncoder

    def getPointsDocument(): Option[PointSchema] = {
        MongoSpark
            .load[PointSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, aPlusDatabaseName, MongoConstants.CollectionPoints)

            )
            .cache()
            .filter(column(SnakeCaseConstants.FullName) === queryOptions.fullName)
            .filter(column(SnakeCaseConstants.CourseId) === queryOptions.courseId)
            .collect()
            .headOption
            .flatMap(row => PointSchema.fromRow(row))
    }

    def getSubmissionIds(pointsData: Option[PointSchema], exerciseIds: Seq[Int]): Map[Int, Seq[Int]] = {
        // returns the submission ids (for the considered user) for the given exercises
        // the returned ids are sorted so that the id for the best submission is the first id in the sequence
        pointsData match {
            case Some(points: PointSchema) => {
                points.modules
                    .map(module => module.exercises.filter(exercise => exerciseIds.contains(exercise.id)))
                    .flatten
                    .map(
                        exercisePoints => (
                            exercisePoints.id,
                            exercisePoints
                                .submissions_with_points
                                .sortWith(
                                    (submission1, submission2) => (
                                        submission1.grade > submission2.grade || (
                                            submission1.grade == submission2.grade &&
                                            submission1.submission_time < submission2.submission_time
                                        )
                                    )
                                )
                                .map(submission => submission.id)
                            )
                    )
                    .toMap
            }
            case None => exerciseIds.map(exerciseId => (exerciseId, Seq.empty)).toMap
        }
    }

    def getExerciseIdFromFileData(
        fileData: FileSchema,
        gitLocations: Map[Int, Option[(String, String, String)]]
    ): Int = {
        gitLocations.find({
            case (_, gitOption) => gitOption match {
                case Some((hostName: String, projectName: String, path: String)) => (
                    hostName == fileData.host_name &&
                    projectName == fileData.project_name &&
                    path == fileData.path
                )
                case None => false
            }
        }) match {
            case Some((exerciseId: Int, _)) => exerciseId
            case None => 0
        }
    }

    def getExerciseIdFromCommitData(
        commitData: CommitSchema,
        gitCommitsWithProjects: Map[Int, Option[(String, String, Seq[String])]]
    ): Int = {
        gitCommitsWithProjects.find({
            case (_, gitOption) => gitOption match {
                case Some((hostName: String, projectName: String, commitIdSeq: Seq[String])) => (
                    hostName == commitData.host_name &&
                    projectName == commitData.project_name &&
                    commitIdSeq.contains(commitData.id)
                )
                case None => false
            }
        }) match {
            case Some((exerciseId: Int, _)) => exerciseId
            case None => 0
        }
    }

    def getSubmissionData(exerciseSubmissionIds: Map[Int, Seq[Int]]): Dataset[(Int, Option[SubmissionSchema])] = {
        // Returns the best submission for the given exercises. The best submission is
        // the first submission id in the list for which the submission contains submission data
        def submissionIds: Seq[Int] = exerciseSubmissionIds.flatMap({case (_, ids) => ids}).toSeq
        def submissionIdIndex: Int => Option[Int] = {
            submissionId => exerciseSubmissionIds
                .find({case (_, subId) => subId.contains(submissionId)}) match {
                    case Some((_, subIds: Seq[Int])) => subIds.zipWithIndex.toMap.get(submissionId)
                    case None => None
                }
            }
        val submissionIndexUfd: UserDefinedFunction = udf[Option[Int], Int](submissionIdIndex)

        val exerciseId: Int => Option[Int] = {
            submissionId => exerciseSubmissionIds
                .find({case (_, subId) => subId.contains(submissionId)})
                .map({case (exerciseId, _) => exerciseId})
        }
        val exerciseIdUdf: UserDefinedFunction = udf[Option[Int], Int](exerciseId)

        def rowToExerciseId(row: Row): Int = {
            row.getInt(row.schema.fieldIndex(SnakeCaseConstants.ExerciseId))
        }

        def getBetterSubmission(row1: Row, row2: Row): Row = {
            val row1Index: Int = row1.getInt(row1.schema.fieldIndex(SnakeCaseConstants.Index))
            val row2Index: Int = row2.getInt(row2.schema.fieldIndex(SnakeCaseConstants.Index))
            row1Index < row2Index match {
                case true => row1
                case false => row2
            }
        }

        MongoSpark
            .load[SubmissionSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, aPlusDatabaseName, MongoConstants.CollectionSubmissions)
            )
            .cache()
            .filter(column(SnakeCaseConstants.Id).isInCollection(submissionIds))
            .filter(column(SnakeCaseConstants.SubmissionData).isNotNull)
            .withColumn(SnakeCaseConstants.Index, submissionIndexUfd(column(SnakeCaseConstants.Id)))
            .withColumn(SnakeCaseConstants.ExerciseId, exerciseIdUdf(column(SnakeCaseConstants.Id)))
            .filter(column(SnakeCaseConstants.Index).isNotNull)
            .filter(column(SnakeCaseConstants.ExerciseId).isNotNull)
            .groupByKey(row => rowToExerciseId(row))
            .reduceGroups((row1, row2) => getBetterSubmission(row1, row2))
            .map({case (exerciseId, row) => (exerciseId, SubmissionSchema.fromRow(row))})
            .cache()
    }

    def getGitProjects(exerciseSubmissionIds: Map[Int, Seq[Int]]): Map[Int, Option[(String, String)]] = {
        // returns the (hostName, projectName) for each exercise
        // uses the first submission id in the list for which the submission contains submission data
        getSubmissionData(exerciseSubmissionIds)
            .collect()
            .toMap
            .mapValues({
                case Some(submission: SubmissionSchema) =>
                    submission.submission_data.git match {
                    case Some(gitSubmission: GitSubmissionSchema) =>
                        (gitSubmission.host_name, gitSubmission.project_name) match {
                            case (Some(hostName: String), Some(projectName: String)) =>
                                Some((hostName, projectName))
                            case _ => None
                        }
                    case None => None
                }
                case None => None
            })
    }

    def getCommitIds(gitLocations: Map[Int, Option[(String, String, String)]]): Map[Int, Seq[String]] = {
        // returns the commit ids for each project with the given path
        val (hostNames: Seq[String], projectNames: Seq[String], paths: Seq[String]) = gitLocations
            .flatMap({
                case (_, gitOption) => gitOption match {
                    case Some((hostName: String, projectName: String, path: String)) => Some((hostName, projectName, path))
                    case None => None
                }}
            )
            .toSeq
            .unzip3

        def getExerciseId(fileData: FileSchema): Int = {
            getExerciseIdFromFileData(fileData, gitLocations)
        }

        MongoSpark
            .load[FileSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, gitlabDatabaseName, MongoConstants.CollectionFiles)
            )
            .cache()
            .filter(column(SnakeCaseConstants.Path).isInCollection(paths))
            .filter(column(SnakeCaseConstants.HostName).isInCollection(hostNames))
            .filter(column(SnakeCaseConstants.ProjectName).isInCollection(projectNames))
            .flatMap(row => FileSchema.fromRow(row))
            .cache()
            .collect()
            .map(file => (getExerciseId(file), file._links match {
                case Some(commitIdList: CommitIdListSchema) => commitIdList.commits match {
                    case Some(commitIds: Seq[String]) => commitIds
                    case None => Seq.empty
                }
                case None => Seq.empty
            }))
            .toMap
    }

    def getCommitData(
        gitCommitsWithProjects: Map[Int, Option[(String, String, Seq[String])]]
    ): Dataset[CommitSchema] = {
        val (hostNames: Seq[String], projectNames: Seq[String], commitIdSeq: Seq[Seq[String]]) = gitCommitsWithProjects
            .flatMap({
                case (_, gitOption) => gitOption match {
                    case Some((hostName: String, projectName: String, commitIds: Seq[String])) =>
                        Some((hostName, projectName, commitIds))
                    case None => None
                }}
            )
            .toSeq
            .unzip3
        val commitIds = commitIdSeq.flatten

        MongoSpark
            .load[CommitSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, gitlabDatabaseName, MongoConstants.CollectionCommits)
            )
            .cache()
            .filter(column(SnakeCaseConstants.HostName).isInCollection(hostNames))
            .filter(column(SnakeCaseConstants.ProjectName).isInCollection(projectNames))
            .filter(column(SnakeCaseConstants.Id).isInCollection(commitIds))
            .orderBy(column(SnakeCaseConstants.CommittedDate).asc)
            .flatMap(row => CommitSchema.fromRow(row))
            .cache()
    }

    def getCommitOutputs(
        gitCommitsWithProjects: Map[Int, Option[(String, String, Seq[String])]]
    ): Map[Int, Seq[CommitOutput]] = {
        // transforms the given commit ids to commit output objects
        def getExerciseId(commitData: CommitSchema): Int = {
            getExerciseIdFromCommitData(commitData, gitCommitsWithProjects)
        }

        getCommitData(gitCommitsWithProjects)
            .collect()
            .map(
                commit => (
                    getExerciseId(commit),
                    CommitOutput(
                        hash = commit.id,
                        message = commit.message,
                        commit_date = commit.committed_date,
                        committer_email = commit.committer_email
                    )
                )
            )
            .groupBy({case (exerciseId, _) => exerciseId})
            .mapValues(outputArray => outputArray.map({case (_, commitOutput) => commitOutput}))
    }

    def getExerciseData(exerciseIds: Seq[Int]): Map[Int, Option[ExerciseSchema]] = {
        MongoSpark
            .load[ExerciseSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, aPlusDatabaseName, MongoConstants.CollectionExercises)
            )
            .cache()
            .filter(column(SnakeCaseConstants.Id).isInCollection(exerciseIds))
            .map(row => (row.getAs[Int](SnakeCaseConstants.Id), ExerciseSchema.fromRow(row)))
            .cache()
            .collect()
            .toMap
    }

    def getExercisePaths(exerciseData: Seq[ExerciseSchema]): Map[Int, Option[String]] = {
        exerciseData
            .map(
                exercise => (
                    exercise.id,
                    exercise.metadata.other match {
                        case Some(otherData: MetadataOtherSchema) => otherData.path
                        case None => None
                    }
                )
            )
            .toMap
    }

    def getGitLocations(
        exercisePaths: Map[Int, Option[String]],
        gitProjects: Map[Int, Option[(String, String)]]
    ): Map[Int, Option[(String, String, String)]] = {
        exercisePaths.map({
            case (exerciseId, pathOption) => (
                exerciseId,
                pathOption match {
                    case Some(path: String) => gitProjects.get(exerciseId) match {
                        case Some(gitProjectOption: Option[(String, String)]) => gitProjectOption match {
                            case Some((hostName: String, projectName: String)) => Some(hostName, projectName, path)
                            case None => None
                        }
                        case None => None
                    }
                    case None => None
                }
            )
        })
    }

    def getCommitIdsWithProjects(
        gitProjects: Map[Int, Option[(String, String)]],
        commitIds: Map[Int, Seq[String]]
    ): Map[Int, Option[(String, String, Seq[String])]] = {
        gitProjects.map({
            case (exerciseId, gitOption) => (
                exerciseId,
                gitOption match {
                    case Some((hostName: String, projectName: String)) => commitIds.get(exerciseId) match {
                        case Some(commitIds: Seq[String]) => Some(hostName, projectName, commitIds)
                        case None => None
                    }
                    case None => None
                }
            )
        })
    }

    def getSimplifiedExerciseNames(exercisePaths: Map[Int,Option[String]]): Map[Int,String] = {
        exercisePaths.mapValues({
            case Some(exercisePath: String) =>
                exercisePath.split(CommonConstants.Slash).lastOption match {
                    case Some(simplifiedName: String) => simplifiedName
                    case None => exercisePath
                }
            case None => CommonConstants.Unknown
        })
    }

    def getExerciseCommitsData(
        pointsData: Option[PointSchema],
        exerciseIds: Seq[Int]
    ): Map[Int, Option[ExerciseCommitsOutput]] = {
        // returns an exercise commits objects for the considered student and the given exerciseId-list
        val exerciseData = getExerciseData(exerciseIds)
        val exercisePaths = getExercisePaths(exerciseData.toSeq.map({case (_, exercise) => exercise}).flatten)
        val submissionIds = getSubmissionIds(pointsData, exerciseIds)
        val gitProjects = getGitProjects(submissionIds)
        val gitLocations = getGitLocations(exercisePaths, gitProjects)
        val gitCommitIds = getCommitIds(gitLocations)
        val commitsWithProjects = getCommitIdsWithProjects(gitProjects, gitCommitIds)
        val commitOutputs = getCommitOutputs(commitsWithProjects)
        val simplifiedExerciseNames = getSimplifiedExerciseNames(exercisePaths)

        commitOutputs.map({
            case (exerciseId, outputs) => (
                exerciseId,
                outputs.isEmpty match {
                    case false => Some(
                        ExerciseCommitsOutput(
                            name = simplifiedExerciseNames.get(exerciseId) match {
                                case Some(exerciseName: String) => exerciseName
                                case None => CommonConstants.Unknown
                            },
                            commit_count = outputs.size,
                            commit_meta = outputs
                        )
                    )
                    case true => None
                }
            )
        })
    }

    def getCourseData(): Option[CourseSchema] = {
        // returns the course information
        MongoSpark
            .load[CourseSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, aPlusDatabaseName, MongoConstants.CollectionCourses)
            )
            .cache()
            .filter(column(SnakeCaseConstants.Id) === queryOptions.courseId)
            .collect()
            .headOption
            .flatMap(row => CourseSchema.fromRow(row))
    }

    def getModuleIds(courseSchemaOption: Option[CourseSchema]): Seq[Int] = {
        courseSchemaOption match {
            case Some(courseSchema: CourseSchema) => {
                courseSchema._links match {
                        case Some(courseLinks: CourseLinksSchema) => {
                            courseLinks.modules match {
                                case Some(modulesIds: Seq[Int]) => modulesIds
                                case None => Seq.empty
                            }
                        }
                        case None => Seq.empty
                    }
            }
            case None => Seq.empty
        }
    }

    def getModuleData(moduleIds: Seq[Int]): Seq[ModuleSchema] = {
        // returns the module information for modules that match the given module ids
        MongoSpark
            .load[ModuleSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, aPlusDatabaseName, MongoConstants.CollectionModules)
            )
            .cache()
            .filter(column(SnakeCaseConstants.Id).isInCollection(moduleIds))
            .filter(column(SnakeCaseConstants.CourseId) === queryOptions.courseId)
            .collect()
            .flatMap(row => ModuleSchema.fromRow(row))
    }

    def getExerciseIds(moduleData: Seq[ModuleSchema]): Map[Int, Seq[Int]] = {
        moduleData
            .map(
                module => (
                    module.id,
                    module._links match {
                        case Some(links: ModuleLinksSchema) => links.exercises match {
                            case Some(exerciseIds: Seq[Int]) => exerciseIds
                            case None => Seq.empty
                        }
                        case None => Seq.empty
                    }
                )
            )
            .toMap
    }

    def getModuleNames(moduleData: Seq[ModuleSchema]): Map[Int,String] = {
        moduleData.map(
            module => (
                module.id,
                module.display_name.number match {
                    case Some(moduleNumber: String) =>
                        moduleNumber.substring(0, moduleNumber.size - 1)
                            .reverse
                            .padTo(2, CommonConstants.ZeroChar)
                            .reverse
                    case None => module.display_name.raw
                }
            )
        )
        .toMap
    }

    def getModuleCommitData(
        moduleDataNames: Map[Int,String],
        exerciseIdMap: Map[Int,Seq[Int]],
        exerciseCommitData: Map[Int,Option[ExerciseCommitsOutput]]
    ): Seq[ModuleCommitsOutput] = {
        exerciseIdMap.toList.map({
            case (moduleId, exerciseIds) => (
                moduleDataNames.getOrElse(moduleId, CommonConstants.Unknown),
                {
                    exerciseIds.map(
                        exerciseId => exerciseCommitData.get(exerciseId) match {
                            case Some(commitDataOption) => commitDataOption match {
                                case Some(commitData: ExerciseCommitsOutput) => Some(commitData)
                                case None => None
                            }
                            case None => None
                        }
                    )
                    .flatten
                    .sortBy(exerciseCommit => exerciseCommit.name)
                }
            )
        })
        .map({case (moduleName, exercises) => ModuleCommitsOutput(moduleName, exercises)})
        .filter(module => module.projects.nonEmpty)
        .sortBy(module => module.module_name)
    }

    def getFullOutput(
        pointsData: Option[PointSchema],
        moduleCommitData: Seq[ModuleCommitsOutput]
    ): FullCourseOutput = {
        pointsData match {
            case Some(points: PointSchema) => FullCourseOutput(
                Seq(
                    StudentCourseOutput.fromSchemas(points, moduleCommitData)
                )
            )
            case None => FullCourseOutput(Seq.empty)
        }
    }

    def getResults(): JsObject = {
        val courseData = getCourseData()
        val moduleIds = getModuleIds(courseData)
        val moduleData = getModuleData(moduleIds)
        val moduleDataNames = getModuleNames(moduleData)
        val exerciseIdMap = getExerciseIds(moduleData)
        val exerciseIds = exerciseIdMap.toSeq.flatMap({case (_, exerciseIds) => exerciseIds})
        val pointsData = getPointsDocument()
        val exerciseCommitData = getExerciseCommitsData(pointsData, exerciseIds)
        val moduleCommitData = getModuleCommitData(moduleDataNames, exerciseIdMap, exerciseCommitData)

        getFullOutput(pointsData, moduleCommitData).toJsObject()
    }
}
