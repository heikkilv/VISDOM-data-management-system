package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.general.model.results.ArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.CoursePointsArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.ExercisePointsArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.FileArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.ModulePointsArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.PipelineReportArtifactResult
import visdom.adapters.general.schemas.CourseSchema
import visdom.adapters.general.schemas.ExerciseAdditionalSchema
import visdom.adapters.general.schemas.ExerciseSchema
import visdom.adapters.general.schemas.FileSchema
import visdom.adapters.general.schemas.PipelineReportSchema
import visdom.adapters.general.schemas.ModuleSchema
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

    def getExercisePoints(): Dataset[ExercisePointsArtifactResult] = {
        val exerciseMetadataMap: Map[Int, ExerciseSchema] =
            modelUtils.getExerciseSchemas()
                .map(exercise => (exercise.id, exercise))
                .collect()
                .toMap
        val exerciseAdditionalMap: Map[Int,ExerciseAdditionalSchema] = modelUtils.getExerciseAdditionalMap()

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
                                exerciseAdditionalMap.getOrElse(exercise.id, ExerciseAdditionalSchema.getEmpty())
                            )
                        )
                    )
                )
            )
            .flatMap(sequence => sequence)
            .flatMap(option => option)
            .map({
                case (userId, lastModified, moduleId, exercise, exerciseMetadata, exerciseAdditionalData) =>
                    ArtifactResult.fromExercisePointsSchema(
                        exercisePointsSchema = exercise,
                        exerciseSchema = exerciseMetadata,
                        additionalSchema = exerciseAdditionalData,
                        moduleId = moduleId,
                        userId = userId,
                        updateTime = lastModified
                    )
            })
    }
}
