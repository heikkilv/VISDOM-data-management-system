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
import visdom.adapters.course.options.CourseDataQueryOptions
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
import visdom.adapters.course.AdapterValues


class CourseDataQuery(queryOptions: CourseDataQueryOptions) {
    val queryCode: Int = 1
    val sparkSession: SparkSession = Session.getSparkSession()
    import sparkSession.implicits.newIntEncoder
    import sparkSession.implicits.newProductEncoder

    def getPointsDocuments(userIds: Seq[Int]): Seq[PointSchema] = {
        MongoSpark
            .load[PointSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, aPlusDatabaseName, MongoConstants.CollectionPoints)

            )
            .cache()
            .filter(column(SnakeCaseConstants.Id).isInCollection(userIds))
            .filter(column(SnakeCaseConstants.CourseId) === queryOptions.courseId)
            .collect()
            .flatMap(row => PointSchema.fromRow(row))
    }

    def getSubmissionIds(pointsData: Seq[PointSchema], exerciseIds: Seq[Int]): Map[(Int, Int), Seq[Int]] = {
        // returns the submission ids for each considered (userId, exerciseId) pair
        // the returned ids are sorted so that the id for the best submission is the first id in the sequence
        pointsData.map(
            points =>
                points.modules
                    .map(module => module.exercises.filter(exercise => exerciseIds.contains(exercise.id)))
                    .flatten
                    .map(
                        exercisePoints => (
                            (points.id, exercisePoints.id),
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
        )
        .reduceOption((map1, map2) => map1 ++ map2) match {
            case Some(result: Map[(Int, Int), Seq[Int]]) => result
            case _ => Map.empty
        }
    }

    def getUserAndExerciseIdFromFileData(
        fileData: FileSchema,
        gitLocations: Map[(Int, Int), Option[(String, String, String)]]
    ): (Int, Int) = {
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
            case Some(((userId: Int, exerciseId: Int), _)) => (userId, exerciseId)
            case None => (0, 0)
        }
    }

    def getExerciseIdFromCommitData(
        commitData: CommitSchema,
        gitCommitsWithProjects: Map[(Int, Int), Option[(String, String, Seq[String])]]
    ): (Int, Int) = {
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
            case Some(((userId: Int, exerciseId: Int), _)) => (userId, exerciseId)
            case None => (0, 0)
        }
    }

    // scalastyle:off method.length
    def getSubmissionData(
        exerciseSubmissionIds: Map[(Int, Int), Seq[Int]]
    ): Dataset[((Int, Int), Option[SubmissionSchema])] = {
        // Returns the best submission for the given (userId, exerciseId) pairs. The best submission is
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
                .map({case ((_, exerciseId), _) => exerciseId})
        }
        val exerciseIdUdf: UserDefinedFunction = udf[Option[Int], Int](exerciseId)

        val userId: Int => Option[Int] = {
            submissionId => exerciseSubmissionIds
                .find({case (_, subId) => subId.contains(submissionId)})
                .map({case ((userId, _), _) => userId})
        }
        val userIdUdf: UserDefinedFunction = udf[Option[Int], Int](userId)

        def rowToUserAndExerciseId(row: Row): (Int, Int) = {
            (
                row.getInt(row.schema.fieldIndex(SnakeCaseConstants.UserId)),
                row.getInt(row.schema.fieldIndex(SnakeCaseConstants.ExerciseId))
            )
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
            .withColumn(SnakeCaseConstants.UserId, userIdUdf(column(SnakeCaseConstants.Id)))
            .withColumn(SnakeCaseConstants.ExerciseId, exerciseIdUdf(column(SnakeCaseConstants.Id)))
            .filter(column(SnakeCaseConstants.Index).isNotNull)
            .filter(column(SnakeCaseConstants.UserId).isNotNull)
            .filter(column(SnakeCaseConstants.ExerciseId).isNotNull)
            .groupByKey(row => rowToUserAndExerciseId(row))
            .reduceGroups((row1, row2) => getBetterSubmission(row1, row2))
            .map({case (identifier, row) => (identifier, SubmissionSchema.fromRow(row))})
            .cache()
    }
    // scalastyle:on method.length

    def getGitProjects(exerciseSubmissionIds: Map[(Int, Int), Seq[Int]]): Map[(Int, Int), Option[(String, String)]] = {
        // returns the (hostName, projectName) for each (userId, exerciseId) pair
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

    def getCommitIds(gitLocations: Map[(Int, Int), Option[(String, String, String)]]): Map[(Int, Int), Seq[String]] = {
        // returns the commit ids for each project with the given path
        val (hostNames: Seq[String], projectNames: Seq[String], paths: Seq[String]) = gitLocations
            .flatMap({
                case (_, gitOption) => gitOption match {
                    case Some((hostName: String, projectName: String, path: String)) =>
                        Some((hostName, projectName, path))
                    case None => None
                }}
            )
            .toSeq
            .unzip3

        def getUserAndExerciseIds(fileData: FileSchema): (Int, Int) = {
            getUserAndExerciseIdFromFileData(fileData, gitLocations)
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
            .map(file => (getUserAndExerciseIds(file), file._links match {
                case Some(commitIdList: CommitIdListSchema) => commitIdList.commits match {
                    case Some(commitIds: Seq[String]) => commitIds
                    case None => Seq.empty
                }
                case None => Seq.empty
            }))
            .toMap
    }

    def getCommitData(
        gitCommitsWithProjects: Map[(Int, Int), Option[(String, String, Seq[String])]]
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
        gitCommitsWithProjects: Map[(Int, Int), Option[(String, String, Seq[String])]]
    ): Map[(Int, Int), Seq[CommitOutput]] = {
        // transforms the given commit ids to commit output objects
        def getUserAndExerciseIds(commitData: CommitSchema): (Int, Int) = {
            getExerciseIdFromCommitData(commitData, gitCommitsWithProjects)
        }

        getCommitData(gitCommitsWithProjects)
            .collect()
            .map(
                commit => (
                    getUserAndExerciseIds(commit),
                    CommitOutput(
                        hash = commit.id,
                        message = commit.message,
                        commit_date = commit.committed_date,
                        committer_email = commit.committer_email
                    )
                )
            )
            .groupBy({case ((userId, exerciseId), _) => (userId, exerciseId)})
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
        gitProjects: Map[(Int, Int), Option[(String, String)]]
    ): Map[(Int, Int), Option[(String, String, String)]] = {
        gitProjects.map({
            case ((userId, exerciseId), gitProject) => (
                (userId, exerciseId),
                exercisePaths.get(exerciseId) match {
                    case Some(pathOption: Option[String]) => pathOption match {
                        case Some(path: String) => gitProject match {
                            case Some((hostName: String, projectName: String)) => Some((hostName, projectName, path))
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
        gitProjects: Map[(Int, Int), Option[(String, String)]],
        commitIds: Map[(Int, Int), Seq[String]]
    ): Map[(Int, Int), Option[(String, String, Seq[String])]] = {
        gitProjects.map({
            case ((userId, exerciseId), gitOption) => (
                (userId, exerciseId),
                gitOption match {
                    case Some((hostName: String, projectName: String)) => commitIds.get((userId, exerciseId)) match {
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
        pointsData: Seq[PointSchema],
        exerciseIds: Seq[Int]
    ): Map[(Int, Int), Option[ExerciseCommitsOutput]] = {
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
            case ((userId, exerciseId), outputs) => (
                (userId, exerciseId),
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
        moduleDataNames: Map[Int, String],
        exerciseIdMap: Map[Int, Seq[Int]],
        exerciseCommitData: Map[(Int, Int), Option[ExerciseCommitsOutput]]
    ): Map[Int, Seq[ModuleCommitsOutput]] = {
        // returns a mapping from userId to a list of module specific commit data
        def getModuleId(exerciseId: Int): Int = {
            exerciseIdMap.find({case (moduleId, exerciseIds) => exerciseIds.contains(exerciseId)}) match {
                case Some((moduleId: Int, _)) => moduleId
                case None => 0
            }
        }

        exerciseCommitData
            .map({
                case ((userId, exerciseId), commitData) => (
                    (userId, getModuleId(exerciseId), exerciseId),
                    commitData
                )
            })
            .groupBy({case ((userId, moduleId, _), _) => (userId, moduleId)})
            .mapValues(data => data.toSeq.map({case (_, commitData) => commitData}))
            .map({
                case ((userId, moduleId), commitData) => (
                    (userId, moduleId),
                    ModuleCommitsOutput(
                        moduleDataNames.getOrElse(moduleId, CommonConstants.Unknown),
                        commitData.flatten
                    )
                )
            })
            .groupBy({case ((userId, _), _) => userId})
            .mapValues(data => data.toSeq.map({case (_, moduleData) => moduleData}))
            .mapValues(
                modules =>
                    modules
                        .filter(module => module.projects.nonEmpty)
                        .sortBy(module => module.module_name)
            )
    }

    def getFullOutput(
        pointsData: Seq[PointSchema],
        moduleCommitData: Map[Int, Seq[ModuleCommitsOutput]]
    ): FullCourseOutput = {
        FullCourseOutput(
            pointsData.map(
                points =>
                    StudentCourseOutput.fromSchemas(points, moduleCommitData.getOrElse(points.id, Seq.empty))
            )
        )
    }

    def getUserIds(courseData: Option[CourseSchema]): Seq[Int] = {
        courseData match {
            case Some(course: CourseSchema) => course._links match {
                case Some(courseLinks: CourseLinksSchema) => courseLinks.points match {
                    case Some(pointLinks: Seq[Int]) => pointLinks
                    case None => Seq.empty
                }
                case None => Seq.empty
            }
            case None => Seq.empty
        }
    }

    def getResults(): JsObject = {
        AdapterValues.cache.getResult(queryCode, queryOptions) match {
            case Some(cachedResult: JsObject) => cachedResult
            case None => {
                val courseData = getCourseData()
                val moduleIds = getModuleIds(courseData)
                val moduleData = getModuleData(moduleIds)
                val moduleDataNames = getModuleNames(moduleData)
                val exerciseIdMap = getExerciseIds(moduleData)
                val exerciseIds = exerciseIdMap.toSeq.flatMap({case (_, exerciseIds) => exerciseIds})
                val userIds = getUserIds(courseData)
                val pointsData = getPointsDocuments(userIds)
                val exerciseCommitData = getExerciseCommitsData(pointsData, exerciseIds)
                val moduleCommitData = getModuleCommitData(moduleDataNames, exerciseIdMap, exerciseCommitData)

                val result = getFullOutput(pointsData, moduleCommitData).toJsObject()
                AdapterValues.cache.addResult(queryCode, queryOptions, result)
                result
            }
        }
    }
}
