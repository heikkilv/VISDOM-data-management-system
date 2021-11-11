package visdom.adapters.course.usecases

import com.mongodb.spark.MongoSpark
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.column
import org.apache.spark.sql.functions.udf
import spray.json.JsObject
import visdom.adapters.course.AdapterValues
import visdom.adapters.course.AdapterValues.aPlusDatabaseName
import visdom.adapters.course.AdapterValues.gitlabDatabaseName
import visdom.adapters.course.schemas.CommitIdListSchema
import visdom.adapters.course.options.HistoryDataQueryOptions
import visdom.adapters.course.output.ExerciseCommitsOutput
import visdom.adapters.course.output.FullHistoryOutput
import visdom.adapters.course.output.ModuleCommitsOutput
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
import visdom.adapters.course.structs.GradeDataCounts
import visdom.adapters.course.structs.GradeCumulativeDataCounts
import visdom.adapters.course.structs.ModuleDataCounts
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils.EnrichedJsObject
import visdom.spark.ConfigUtils
import visdom.spark.Session
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.CourseUtils

// TODO: This file contains a lot copy-pasted code from CourseDataQuery. Restructure them properly


class HistoryDataQuery(queryOptions: HistoryDataQueryOptions) {
    val queryCode: Int = 3
    val currentTime: String = GeneralUtils.getCurrentTimeString()
    val sparkSession: SparkSession = Session.getSparkSession()
    import sparkSession.implicits.newIntEncoder
    import sparkSession.implicits.newProductEncoder

    def getPointsDocuments(moduleIds: Seq[Int]): Seq[PointSchema] = {
        MongoSpark
            .load[PointSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, aPlusDatabaseName, MongoConstants.CollectionPoints)

            )
            .cache()
            .filter(column(SnakeCaseConstants.CourseId) === queryOptions.courseId)
            .collect()
            .flatMap(row => PointSchema.fromRow(row))
            .map(points => points.withModules(points.modules.filter(module => moduleIds.contains(module.id))))
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

    def getExerciseCommitsIds(
        pointsData: Seq[PointSchema],
        exerciseIds: Seq[Int]
    ): Map[(Int, Int), Seq[String]] = {
        // returns an exercise commits objects for the considered student and the given exerciseId-list

        // mapping from exercise id to exercise document
        val exerciseData: Map[Int,Option[ExerciseSchema]] = getExerciseData(exerciseIds)
        // mapping from exercise id to GitLab path
        val exercisePaths: Map[Int,Option[String]] =
            getExercisePaths(exerciseData.toSeq.map({case (_, exercise) => exercise}).flatten)
        // mapping from (user id, exercise id) pair to list of submissions ids
        val submissionIds: Map[(Int, Int),Seq[Int]] = getSubmissionIds(pointsData, exerciseIds)
        // mapping from (user id, exercise id) pair to GitLab (host name, project name) pair
        val gitProjects: Map[(Int, Int),Option[(String, String)]] = getGitProjects(submissionIds)
        // mapping from (user id, exercise id) pair to GitLab (host name, project name, path) tuple
        val gitLocations: Map[(Int, Int),Option[(String, String, String)]] =
            getGitLocations(exercisePaths, gitProjects)
        // return a mapping from (user id, exercise id) pair to Gitlab commit list
        getCommitIds(gitLocations)
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

    private def filterModuleData(moduleData: Seq[ModuleSchema]): Seq[ModuleSchema] = {
        val moduleNumbers: Seq[(Int, Int)] = moduleData.map(
            module => (
                module.id,
                module.display_name.number match {
                    // the module numbers should end in a dot, e.g. "1.", "2.", ...
                    case Some(moduleNumber: String) => GeneralUtils.toInt(moduleNumber.dropRight(1)) match {
                        case Some(number: Int) => number
                        case None => 0
                    }
                    case None => 0
                }
            )
        )
        val consideredModuleIds: Seq[Int] =
            moduleNumbers
                .filter({case (_, moduleNumber) => moduleNumber > 0})
                .map({case (moduleId, _) => moduleId})

        moduleData.filter(module => consideredModuleIds.contains(module.id))
    }

    def getModuleData(moduleIds: Seq[Int]): Seq[ModuleSchema] = {
        // returns the module information for modules that match the given module ids
        val moduleData: Seq[ModuleSchema] = MongoSpark
            .load[ModuleSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, aPlusDatabaseName, MongoConstants.CollectionModules)
            )
            .cache()
            .filter(column(SnakeCaseConstants.Id).isInCollection(moduleIds))
            .filter(column(SnakeCaseConstants.CourseId) === queryOptions.courseId)
            .collect()
            .flatMap(row => ModuleSchema.fromRow(row))

        filterModuleData(moduleData)
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
            // only include modules that contain at least one exercise
            .filter({case (_, exerciseIds) => exerciseIds.nonEmpty})
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

    def getModuleCommitCounts(
        exerciseIdMap: Map[Int, Seq[Int]],
        exerciseCommitsIds: Map[(Int, Int), Seq[String]]
    ): Map[(Int, Int), Int] = {
        def getModuleId(exerciseId: Int): Option[Int] = {
            exerciseIdMap
                .find({case (_, exerciseIds) => exerciseIds.contains(exerciseId)})
                .headOption
                .map({case (moduleId, _) => moduleId})
        }

        exerciseCommitsIds.map({
            case ((userId, exerciseId), commitIds) =>
                getModuleId(exerciseId).map(moduleId =>((userId, moduleId, exerciseId), commitIds))
            })
            .flatten
            .groupBy({case ((userId, moduleId, _), _) => (userId, moduleId)})
            .mapValues(
                iterable =>
                    iterable
                        .map({case (_, commitIds) => commitIds})
                        .reduceOption((commitIds1, commitIds2) => commitIds1 ++ commitIds2)
                        .map(commitIds => commitIds.toSet.size) match {
                            case Some(commitCount: Int) => commitCount
                            case None => 0
                        }
            )
    }

    def getUserModuleData(
        moduleIds: Seq[Int],
        pointsData: Seq[PointSchema],
        moduleCommitCounts: Map[(Int, Int), Int]
    ): Map[(Int, Int), ModuleDataCounts[Int]] = {
        pointsData
            .map(points => points.id)
            // create a full list of user-module pairs
            .flatMap(userId => moduleIds.map(moduleId => (userId, moduleId)))
            // add the commit count data to the user-module pairs
            .map(
                userModulePair => (
                    userModulePair,
                    moduleCommitCounts.get(userModulePair) match {
                        case Some(commitCount: Int) => commitCount
                        case None => 0
                    }
                )
            )
            // combine the commit count data with the module points data for each user-module pair
            .map({
                case ((userId, moduleId), commits) =>
                    pointsData
                        .find(pointsSchema => pointsSchema.id == userId)
                        .map(
                            pointSchema => pointSchema.modules.find(moduleSchema => moduleSchema.id == moduleId)
                        )
                        .flatten
                        .map(moduleSchema => ((userId, moduleId), (moduleSchema, commits)))
            })
            .flatten
            .map({case (userModulePair, (moduleSchema, commits)) =>
                (
                    userModulePair,
                    ModuleDataCounts(
                        points = moduleSchema.points,
                        exercises = moduleSchema.exercises.count(exercise => exercise.points > 0),
                        submissions = moduleSchema.submission_count,
                        commits = commits
                    )
                )
            })
            .toMap
    }

    def getUserWeekData(
        moduleDataNames: Map[Int, String],
        userModuleData: Map[(Int, Int), ModuleDataCounts[Int]]
    ): Map[(Int, String), ModuleDataCounts[Int]] = {
        userModuleData
            .map({
                case ((userId, moduleId), data) =>
                    moduleDataNames
                        .get(moduleId)
                        .map(weekNumber => ((userId, moduleId, weekNumber), data))
            })
            .flatten
            .groupBy({case ((userId, _, weekNumber), _) => (userId, weekNumber)})
            .mapValues(
                counts =>
                    counts
                        .map({case (_, data) => data})
                        .reduceOption((data1, data2) => data1.add(data2)) match {
                            case Some(dataCounts: ModuleDataCounts[_]) => dataCounts
                            case None => ModuleDataCounts.getEmpty()
                        }
            )
    }

    def getTotalMaxPoints(exerciseIds: Seq[Int]): Int = {
        getExerciseData(exerciseIds)
            .map({
                case (_, exerciseOption) => exerciseOption match {
                    case Some(exercise: ExerciseSchema) => exercise.max_points
                    case None => 0
                }
            })
            .sum
    }

    def getPredictedStudentGrades(pointsData: Seq[PointSchema], exerciseIds: Seq[Int]): Map[Int, Int] = {
        val courseTotalPoints: Int = getTotalMaxPoints(exerciseIds)
        pointsData
            .map(points => (points.id, CourseUtils.getPredictedGrade(courseTotalPoints, points.points)))
            .toMap
    }

    def addGradesToWeekData(
        userWeekData: Map[(Int, String), ModuleDataCounts[Int]],
        studentGradeMap: Map[Int, Int]
    ): Map[(Int, Int, String), ModuleDataCounts[Int]] = {
        userWeekData
            // add the predicted grade to the user-specific cumulative data
            .map({
                case ((userId, week), weekData) =>
                    (
                        (
                            userId,
                            studentGradeMap
                                .find({case (gradeMapStudentId, _) => gradeMapStudentId == userId})
                                .map({case (_, grade) => grade}) match {
                                    case Some(grade: Int) => grade
                                    case None => 0
                                },
                            week
                        ),
                        weekData
                    )
            })
    }

    def getGradeData(
        userWeekData: Map[(Int, String), ModuleDataCounts[Int]],
        studentGradeMap: Map[Int, Int]
    ): Map[Int, GradeDataCounts] = {
        addGradesToWeekData(userWeekData, studentGradeMap)
            // group the data first by grade and then by week and calculate user averages
            .groupBy({case ((_, grade, _), _) => grade})
            .mapValues(
                dataMap =>
                    dataMap
                        .groupBy({case ((_, _, week), weekData) => week})
                        .mapValues(
                            weekDataMap => (
                                weekDataMap.size,
                                ModuleDataCounts.getAverages(
                                    weekDataMap.map({case (_, weekData) => weekData}).toSeq
                                )
                            )
                        )
            )
            // calculate the number of students for each grade
            .mapValues(
                weekDataMap => GradeDataCounts(
                    students =
                        weekDataMap
                            .map({case (_, (count, _)) => count})
                            .fold(0)((count1, count2) => math.max(count1, count2)),
                    weeks = weekDataMap.mapValues({case (_, weekData) => weekData})
                )
            )
    }

    def getResults(): JsObject = {
        AdapterValues.cache.getResult(queryCode, queryOptions) match {
            case Some(cachedResult: JsObject) => {
                println(s"Using result from cache for query ${queryCode} with ${queryOptions}")
                cachedResult
            }
            case None => {
                val courseData: Option[CourseSchema] = getCourseData()
                val moduleData: Seq[ModuleSchema] = getModuleData(getModuleIds(courseData))
                val moduleDataNames: Map[Int, String] = getModuleNames(moduleData)
                val exerciseIdMap: Map[Int, Seq[Int]] = getExerciseIds(moduleData)
                val moduleIds: Seq[Int] = exerciseIdMap.keySet.toSeq
                val exerciseIds: Seq[Int] = exerciseIdMap.toSeq.flatMap({case (_, exerciseIds) => exerciseIds})
                val pointsData: Seq[PointSchema] = getPointsDocuments(moduleIds)
                val exerciseCommitIds: Map[(Int, Int), Seq[String]] = getExerciseCommitsIds(pointsData, exerciseIds)
                val moduleCommitCounts: Map[(Int, Int), Int] =
                    getModuleCommitCounts(exerciseIdMap, exerciseCommitIds)
                val userModuleData: Map[(Int, Int), ModuleDataCounts[Int]] =
                    getUserModuleData(moduleIds, pointsData, moduleCommitCounts)
                val userWeekData: Map[(Int, String), ModuleDataCounts[Int]] =
                    getUserWeekData(moduleDataNames, userModuleData)
                val studentGradeMap: Map[Int, Int] = getPredictedStudentGrades(pointsData, exerciseIds)
                val gradeData: Map[Int, GradeDataCounts] = getGradeData(userWeekData, studentGradeMap)
                val fullGradeData: Map[Int, GradeDataCounts] = GradeDataCounts.fillMissingData(gradeData)
                val fullCumulativeGradeData: Map[Int, GradeCumulativeDataCounts] =
                    fullGradeData.mapValues(gradeData => GradeCumulativeDataCounts.fromGradeDataCounts(gradeData))

                val result: JsObject =
                    FullHistoryOutput.fromGradeWeekData(fullCumulativeGradeData)
                    .toJsObject()
                    .sort()

                AdapterValues.cache.addResult(queryCode, queryOptions, result)
                result
            }
        }
    }
}
