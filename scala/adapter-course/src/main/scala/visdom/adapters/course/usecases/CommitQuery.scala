package visdom.adapters.course.usecases

import com.mongodb.spark.MongoSpark
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.column
import org.apache.spark.sql.functions.udf
import spray.json.JsObject
import visdom.adapters.course.AdapterValues.aPlusDatabaseName
import visdom.adapters.course.AdapterValues.gitlabDatabaseName
import visdom.adapters.course.options.CommitQueryOptions
import visdom.adapters.course.schemas.CommitSchema
import visdom.adapters.course.schemas.CourseLinksSchema
import visdom.adapters.course.schemas.CourseSchema
import visdom.adapters.course.schemas.ExercisePointsSchema
import visdom.adapters.course.schemas.ExerciseSchema
import visdom.adapters.course.schemas.FileSchema
import visdom.adapters.course.schemas.GitSubmissionSchema
import visdom.adapters.course.schemas.ModuleSchema
import visdom.adapters.course.schemas.PointSchema
import visdom.adapters.course.schemas.SubmissionSchema
import visdom.adapters.course.output.CommitOutput
import visdom.adapters.course.output.ExerciseCommitsOutput
import visdom.database.mongodb.MongoConstants
import visdom.spark.ConfigUtils
import visdom.spark.Session
import visdom.utils.CommonConstants
import visdom.utils.SnakeCaseConstants


class CommitQuery(queryOptions: CommitQueryOptions) {
    val sparkSession: SparkSession = Session.getSparkSession()
    import sparkSession.implicits.newProductEncoder
    // sparkSession.sparkContext.hadoopConfiguration.

    def getSubmissionIds(): Seq[Int] = {
        // returns the submission ids (for this user-exercise pair)
        // the returned ids are sorted so that the id for the best submission is the first id in the sequence
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
            .flatMap(row => PointSchema.fromRow(row)) match {
                case Some(points: PointSchema) => {
                    points.modules
                        .map(module => module.exercises.filter(exercise => exercise.id == queryOptions.exerciseId))
                        .flatten
                        .headOption match {
                            case Some(exercisePoints: ExercisePointsSchema) => {
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
                            }
                            case None => Seq.empty
                        }
                }
                case None => Seq.empty
        }
    }

    def getGitProject(submissionIds: Seq[Int]): Option[(String, String)] = {
        // returns the (hostName, projectName)
        // uses the first submission id in the list for which the submission contains submission data

        def submissionIdIndex: Int => Option[Int] = {
            submissionId => submissionIds.zipWithIndex.toMap.get(submissionId)
        }
        val indexUfd: UserDefinedFunction = udf[Option[Int], Int](submissionIdIndex)

        MongoSpark
            .load[SubmissionSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, aPlusDatabaseName, MongoConstants.CollectionSubmissions)
            )
            .cache()
            .filter(column(SnakeCaseConstants.Id).isInCollection(submissionIds))
            .filter(column(SnakeCaseConstants.SubmissionData).isNotNull)
            .withColumn(SnakeCaseConstants.Index, indexUfd(column(SnakeCaseConstants.Id)))
            .filter(column(SnakeCaseConstants.Index).isNotNull)
            .orderBy(column(SnakeCaseConstants.Index))
            .collect()
            .headOption
            .flatMap(row => SubmissionSchema.fromRow(row))
            .map(submission => submission.submission_data.git)
            .flatten match {
                case Some(gitSubmission: GitSubmissionSchema) => {
                    (gitSubmission.host_name, gitSubmission.project_name) match {
                        case (Some(hostName: String), Some(projectName: String)) =>
                            Some((hostName, projectName))
                        case _ => None
                    }
                }
                case None => None
            }
    }

    def getCommitIds(exercisePath: String, gitProject: Option[(String, String)]): Seq[String] = {
        // returns the commit ids for the given path and project
        gitProject match {
            case Some((hostName: String, projectName: String)) => MongoSpark
                .load[FileSchema](
                    sparkSession,
                    ConfigUtils.getReadConfig(sparkSession, gitlabDatabaseName, MongoConstants.CollectionFiles)
                )
                .cache()
                .filter(column(SnakeCaseConstants.Path) === exercisePath)
                .filter(column(SnakeCaseConstants.ProjectName) === projectName)
                .filter(column(SnakeCaseConstants.HostName) === hostName)
                .collect()
                .headOption
                .flatMap(row => FileSchema.fromRow(row))
                .map(file => file._links)
                .flatten
                .map(commitList => commitList.commits)
                .flatten match {
                    case Some(commitIds: Seq[String]) => commitIds
                    case None => Seq.empty
                }
            case None => Seq.empty
        }
    }

    def getCommitOutputs(gitProject: Option[(String, String)], commitIds: Seq[String]): Seq[CommitOutput] = {
        // transforms the given commit ids to commit output objects
        gitProject match {
            case Some((hostName: String, projectName: String)) => MongoSpark
                .load[CommitSchema](
                    sparkSession,
                    ConfigUtils.getReadConfig(sparkSession, gitlabDatabaseName, MongoConstants.CollectionCommits)
                )
                .cache()
                .filter(column(SnakeCaseConstants.ProjectName) === projectName)
                .filter(column(SnakeCaseConstants.HostName) === hostName)
                .filter(column(SnakeCaseConstants.Id).isInCollection(commitIds))
                .orderBy(column(SnakeCaseConstants.CommittedDate).asc)
                .collect()
                .flatMap(row => CommitSchema.fromRow(row))
                .map(
                    commit =>
                        CommitOutput(
                            hash = commit.id,
                            message = commit.message,
                            commit_date = commit.committed_date,
                            committer_email = commit.committer_email
                        )
                )
            case None => Seq.empty
        }
    }

    def getExerciseCommitsData(): Option[ExerciseCommitsOutput] = {
        // returns an exercise commits object for the exercise-student pair
        MongoSpark
            .load[ExerciseSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, aPlusDatabaseName, MongoConstants.CollectionExercises)
            )
            .cache()
            .filter(column(SnakeCaseConstants.Id) === queryOptions.exerciseId)
            .collect()
            .headOption
            .flatMap(row => ExerciseSchema.fromRow(row))
            .map(exercise => exercise.metadata.other.map(folder => folder.path))
            .flatten match {
                case Some(exercisePathOption: Option[String]) => exercisePathOption match {
                    case Some(exercisePath: String) => {
                        val gitProject: Option[(String, String)] = getGitProject(getSubmissionIds())
                        val commitOutputs: Seq[CommitOutput] =
                            getCommitOutputs(gitProject, getCommitIds(exercisePath, gitProject))

                        Some(
                            ExerciseCommitsOutput(
                                name = exercisePath.split(CommonConstants.Slash).lastOption match {
                                    case Some(simplifiedName: String) => simplifiedName
                                    case None => exercisePath
                                },
                                commit_count = commitOutputs.size,
                                commit_meta = commitOutputs
                            )
                        )
                    }
                    case None => None
                }
                case None => None
            }
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

    def getResults(): JsObject = {
        val courseData = getCourseData()
        val moduleIds = getModuleIds(courseData)
        val moduleData = getModuleData(moduleIds)
        println(courseData)
        moduleData.foreach(module => println(module))

        getExerciseCommitsData() match {
            case Some(exerciseData: ExerciseCommitsOutput) => exerciseData.toJsObject()
            case None => JsObject()
        }
    }
}
