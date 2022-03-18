package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.general.model.results.MetadataResult
import visdom.adapters.general.model.results.MetadataResult.CourseMetadataResult
import visdom.adapters.general.model.results.MetadataResult.ExerciseMetadataResult
import visdom.adapters.general.model.results.MetadataResult.ModuleMetadataResult
import visdom.adapters.general.schemas.ExerciseAdditionalSchema
import visdom.adapters.general.schemas.ModuleAdditionalSchema
import visdom.adapters.general.schemas.PointsSchema


class ModelMetadataUtils(sparkSession: SparkSession, modelUtils: ModelUtils) {
    import sparkSession.implicits.newProductEncoder

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

    def getCourseMetadata(): Dataset[CourseMetadataResult] = {
        modelUtils.getCourseSchemas()
            .map(schema => MetadataResult.fromCourseSchema(schema))
    }

    def getModuleMetadata(): Dataset[ModuleMetadataResult] = {
        val additionalSchemaMap: Map[Int, ModuleAdditionalSchema] = getModuleAdditionalMap()

        modelUtils.getModuleSchemas()
            .flatMap(schema => additionalSchemaMap.get(schema.id).map(additionalSchema => (schema, additionalSchema)))
            .map({case (schema, additionalSchema) => MetadataResult.fromModuleSchema(schema, additionalSchema)})
    }

    def getExerciseMetadata(): Dataset[ExerciseMetadataResult] = {
        val additionalSchemaMap: Map[Int, ExerciseAdditionalSchema] = modelUtils.getExerciseAdditionalMap()

        modelUtils.getExerciseSchemas()
            .flatMap(schema => additionalSchemaMap.get(schema.id).map(additionalSchema => (schema, additionalSchema)))
            .map({case (schema, additionalSchema) => MetadataResult.fromExerciseSchema(schema, additionalSchema)})
    }
}
