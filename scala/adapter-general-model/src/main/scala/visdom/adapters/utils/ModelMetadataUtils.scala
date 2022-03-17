package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import visdom.adapters.general.model.results.MetadataResult
import visdom.adapters.general.model.results.MetadataResult.CourseMetadataResult
import visdom.adapters.general.model.results.MetadataResult.ExerciseMetadataResult
import visdom.adapters.general.model.results.MetadataResult.ModuleMetadataResult
import visdom.adapters.general.schemas.CourseSchema
import visdom.adapters.general.schemas.ExerciseAdditionalSchema
import visdom.adapters.general.schemas.ExerciseSchema
import visdom.adapters.general.schemas.ModuleSchema
import visdom.adapters.general.schemas.ModuleAdditionalSchema
import visdom.adapters.general.schemas.ModuleMetadataOtherSchema
import visdom.adapters.general.schemas.PointsSchema
import visdom.database.mongodb.MongoConstants


class ModelMetadataUtils(sparkSession: SparkSession, modelUtils: ModelUtils) {
    import sparkSession.implicits.newProductEncoder

    def getModuleSchemas(): Dataset[ModuleSchema] = {
        modelUtils.loadMongoDataAplus[ModuleSchema](MongoConstants.CollectionModules)
            .flatMap(row => ModuleSchema.fromRow(row))
            .persist(StorageLevel.MEMORY_ONLY)
    }

    def getModuleDatesMap(): Map[Int, (Option[String], Option[String])] = {
        getModuleSchemas()
            .map(
                schema => (
                    schema.id,
                    schema.metadata.other match {
                        case Some(otherSchema: ModuleMetadataOtherSchema) => (
                            Some(otherSchema.start_date),
                            Some(otherSchema.end_date)
                        )
                        case None => (None, None)
                    }
                )
            )
            .collect()
            .toMap
    }

    def getModuleAdditionalMap(): Map[Int, ModuleAdditionalSchema] = {
        val points: Dataset[PointsSchema] = modelUtils.getPointsSchemas()

        points.isEmpty match {
            case true => Map.empty
            case false =>
                points
                    .head()
                    .modules
                    .map(
                        moduleSchema => (
                            moduleSchema.id,
                            ModuleAdditionalSchema(
                                max_points = moduleSchema.max_points,
                                points_to_pass = moduleSchema.points_to_pass
                            )
                        )
                    )
                    .toMap
        }
    }

    def getExerciseAdditionalMap(): Map[Int, ExerciseAdditionalSchema] = {
        val moduleDatesMap: Map[Int, (Option[String], Option[String])] = getModuleDatesMap()
        val points: Dataset[PointsSchema] = modelUtils.getPointsSchemas()

        points.isEmpty match {
            case true => Map.empty
            case false =>
                points
                    .head()
                    .modules
                    .map(
                        module => module.exercises.map(
                            exercise => (
                                exercise.id,
                                (
                                    exercise.difficulty,
                                    exercise.points_to_pass,
                                    moduleDatesMap.getOrElse(module.id, (None, None))
                                )
                            )
                        )
                    )
                    .flatten
                    .map({case (exerciseId, (difficulty, pointsToPass, (startDate, endDate))) => (
                        exerciseId,
                        ExerciseAdditionalSchema(
                            difficulty = Some(difficulty),
                            points_to_pass = Some(pointsToPass),
                            start_date = startDate,
                            end_date = endDate
                        )
                    )})
                    .toMap
        }
    }

    def getCourseMetadata(): Dataset[CourseMetadataResult] = {
        modelUtils.loadMongoDataAplus[CourseSchema](MongoConstants.CollectionCourses)
            .flatMap(row => CourseSchema.fromRow(row))
            .map(schema => MetadataResult.fromCourseSchema(schema))
    }

    def getModuleMetadata(): Dataset[ModuleMetadataResult] = {
        val additionalSchemaMap: Map[Int, ModuleAdditionalSchema] = getModuleAdditionalMap()

        getModuleSchemas()
            .flatMap(schema => additionalSchemaMap.get(schema.id).map(additionalSchema => (schema, additionalSchema)))
            .map({case (schema, additionalSchema) => MetadataResult.fromModuleSchema(schema, additionalSchema)})
    }

    def getExerciseMetadata(): Dataset[ExerciseMetadataResult] = {
        val additionalSchemaMap: Map[Int, ExerciseAdditionalSchema] = getExerciseAdditionalMap()

        modelUtils.loadMongoDataAplus[ExerciseSchema](MongoConstants.CollectionExercises)
            .flatMap(row => ExerciseSchema.fromRow(row))
            .flatMap(schema => additionalSchemaMap.get(schema.id).map(additionalSchema => (schema, additionalSchema)))
            .map({case (schema, additionalSchema) => MetadataResult.fromExerciseSchema(schema, additionalSchema)})
    }
}
