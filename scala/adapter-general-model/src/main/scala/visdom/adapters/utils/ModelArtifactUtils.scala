package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.results.ArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.CoursePointsArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.ExercisePointsArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.FileArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.ModulePointsArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.PipelineReportArtifactResult
import visdom.adapters.general.schemas.CourseSchema
import visdom.adapters.general.schemas.ExerciseAdditionalSchema
import visdom.adapters.general.schemas.ExerciseSchema
import visdom.adapters.general.schemas.FileIdSchema
import visdom.adapters.general.schemas.FileSchema
import visdom.adapters.general.schemas.ModuleSchema
import visdom.adapters.general.schemas.PipelineReportSchema
import visdom.adapters.general.schemas.SubmissionGitDataSchema
import visdom.database.mongodb.MongoConstants
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants


class ModelArtifactUtils(sparkSession: SparkSession, modelUtils: ModelUtils) {
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newSequenceEncoder

    def getFiles(): Dataset[FileArtifactResult] = {
        val allFiles: Dataset[FileSchema] =
            modelUtils.loadMongoDataGitlab[FileSchema](MongoConstants.CollectionFiles)
                .flatMap(row => FileSchema.fromRow(row))

        val fileParentMap: Map[(String, String, String), Seq[String]] =
            allFiles
                .map(
                    fileSchema => (
                        fileSchema,
                        GeneralUtils.getUpperFolder(fileSchema.path)
                    )
                )
                .filter(schemaWithParentFolder => schemaWithParentFolder._2 != CommonConstants.EmptyString)
                .groupByKey({case (schema, parentFolder) => (schema.host_name, schema.project_name, parentFolder)})
                .mapValues({case (schema, _) => Seq(schema.path)})
                .reduceGroups((first, second) => first ++ second)
                .collect()
                .toMap

        allFiles
            .map(
                fileSchema => ArtifactResult.fromFileSchema(
                    fileSchema,
                    fileSchema.`type` == SnakeCaseConstants.Tree match {
                        case true => fileParentMap.getOrElse(
                            (fileSchema.host_name, fileSchema.project_name, fileSchema.path),
                            Seq.empty
                        )
                        case false => Seq.empty
                    }
                )
            )
    }

    def getPipelineReports(): Dataset[PipelineReportArtifactResult] = {
        val projectNames: Map[Int, String] = modelUtils.getProjectNameMap()

        modelUtils.loadMongoDataGitlab[PipelineReportSchema](MongoConstants.CollectionPipelineReports)
            .flatMap(row => PipelineReportSchema.fromRow(row))
            // include only the reports that have a known project name
            .filter(report => projectNames.keySet.contains(report.pipeline_id))
            .map(
                reportSchema =>
                    ArtifactResult.fromPipelineReportSchema(
                        reportSchema,
                        projectNames.getOrElse(reportSchema.pipeline_id, CommonConstants.EmptyString)
                    )
            )
    }

    def getCoursePoints(): Dataset[CoursePointsArtifactResult] = {
        val courseMetadata: Map[Int, CourseSchema] =
            modelUtils.getCourseSchemas()
                .map(course => (course.id, course))
                .collect()
                .toMap

        modelUtils.getPointsSchemas()
            .map(points => ArtifactResult.fromCoursePointsSchema(points, courseMetadata.get(points.course_id)))
    }

    def getModulePoints(): Dataset[ModulePointsArtifactResult] = {
        val moduleMetadataMap: Map[Int, ModuleSchema] =
            modelUtils.getModuleSchemas()
                .map(module => (module.id, module))
                .collect()
                .toMap

        modelUtils.getPointsSchemas()
            .flatMap(
                points => points.modules.map(
                    module => moduleMetadataMap.get(module.id).map(
                        moduleMetadata => (points.id, points.metadata.last_modified, module, moduleMetadata)
                    )
                )
            )
            .flatMap(option => option)
            .map({
                case (userId, lastModified, module, moduleMetadata) =>
                    ArtifactResult.fromModulePointsSchema(
                        modulePointsSchema = module,
                        moduleSchema = moduleMetadata,
                        userId = userId,
                        updateTime = lastModified
                    )
            })
    }

    def getSubmissionFileMap(): Map[Int, FileIdSchema] = {
        // returns a mapping from submission id to file for those submissions that have GitLab content
        val exerciseGitMap: Map[Int, String] = modelUtils.getExerciseGitMap()

        modelUtils.getSubmissionSchemas()
            .flatMap(
                submission => submission.submission_data.map(submissionData => submissionData.git).flatten match {
                    case Some(gitData: SubmissionGitDataSchema) =>
                        exerciseGitMap.get(submission.exercise.id) match {
                            case Some(gitPath: String) => Some(
                                (
                                    submission.id,
                                    FileIdSchema(
                                        path = gitPath,
                                        project_name = gitData.project_name,
                                        host_name = gitData.host_name
                                    )
                                )
                            )
                            case None => None
                        }
                    case None => None
                }
            )
            .collect()
            .toMap
    }

    def getFileCommitMap(fileIds: Seq[FileIdSchema]): Map[FileIdSchema, Seq[ItemLink]] = {
        // returns a mapping from files to a list of commit event links
        modelUtils.loadMongoDataGitlab[FileSchema](MongoConstants.CollectionFiles)
            .flatMap(row => FileSchema.fromRow(row))
            .map(
                file => (
                    FileIdSchema(
                        path = file.path,
                        project_name = file.project_name,
                        host_name = file.host_name
                    ),
                    file
                )
            )
            .filter(fileWithIdSchema => fileIds.contains(fileWithIdSchema._1))
            .map({
                case (fileIdSchema, file) => (
                    fileIdSchema,
                    file._links.map(
                        links => links.commits.map(
                            commits => commits.map(
                                commit => ItemLink(
                                    CommitEvent.getId(file.host_name, file.project_name, commit),
                                    CommitEvent.CommitEventType
                                )
                            )
                        )
                    ).flatten match {
                        case Some(commitEventIds: Seq[ItemLink]) => commitEventIds
                        case None => Seq.empty
                    }
                )
            })
            .collect()
            .toMap
    }

    def getSubmissionCommitMap(): Map[Int, Seq[ItemLink]] = {
        // returns a mapping from submission id to a list of commit event links
        val submissionFileMap: Map[Int, FileIdSchema] = getSubmissionFileMap()
        val allSubmissionFiles: Seq[FileIdSchema] = submissionFileMap.map({case (_, file) => file}).toSeq.distinct
        val fileCommitMap: Map[FileIdSchema, Seq[ItemLink]] = getFileCommitMap(allSubmissionFiles)

        submissionFileMap
            .map({
                case (submissionId, fileIdSchema) => (
                    submissionId,
                    fileCommitMap.get(fileIdSchema) match {
                        case Some(commits: Seq[ItemLink]) => commits
                        case None => Seq.empty
                    }
                )
            })
            .filter({case (_, commits) => commits.size > 0})
    }

    def getExercisePoints(): Dataset[ExercisePointsArtifactResult] = {
        val exerciseMetadataMap: Map[Int, ExerciseSchema] =
            modelUtils.getExerciseSchemas()
                .map(exercise => (exercise.id, exercise))
                .collect()
                .toMap
        val exerciseAdditionalMap: Map[Int, ExerciseAdditionalSchema] = modelUtils.getExerciseAdditionalMap()

        val submissionCommitMap: Map[Int, Seq[ItemLink]] = getSubmissionCommitMap()

        modelUtils.getPointsSchemas()
            .flatMap(
                points => points.modules.map(
                    module => module.exercises.map(
                        exercise => exerciseMetadataMap.get(exercise.id).map(
                            exerciseMetadata => (
                                points.id,
                                points.metadata.last_modified,
                                module.id,
                                exercise,
                                exerciseMetadata,
                                exerciseAdditionalMap.getOrElse(exercise.id, ExerciseAdditionalSchema.getEmpty()),
                                exercise.submissions_with_points
                                    .map(submission => submissionCommitMap.getOrElse(submission.id, Seq.empty))
                                    .flatten
                                    .distinct
                            )
                        )
                    )
                )
            )
            .flatMap(sequence => sequence)
            .flatMap(option => option)
            .map({
                case (userId, lastModified, moduleId, exercise, exerciseMetadata, exerciseAdditionalData, commits) =>
                    ArtifactResult.fromExercisePointsSchema(
                        exercisePointsSchema = exercise,
                        exerciseSchema = exerciseMetadata,
                        additionalSchema = exerciseAdditionalData,
                        moduleId = moduleId,
                        userId = userId,
                        relatedCommitEventLinks = commits,
                        updateTime = lastModified
                    )
            })
    }
}
