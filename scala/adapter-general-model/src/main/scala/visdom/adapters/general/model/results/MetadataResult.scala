package visdom.adapters.general.model.results

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.base.Metadata
import visdom.adapters.general.model.metadata.CourseMetadata
import visdom.adapters.general.model.metadata.ExerciseMetadata
import visdom.adapters.general.model.metadata.ModuleMetadata
import visdom.adapters.general.model.metadata.data.CourseData
import visdom.adapters.general.model.metadata.data.ExerciseData
import visdom.adapters.general.model.metadata.data.ModuleData
import visdom.adapters.general.schemas.CourseSchema
import visdom.adapters.general.schemas.ExerciseSchema
import visdom.adapters.general.schemas.ModuleSchema
import visdom.adapters.general.schemas.ModuleAdditionalSchema
import visdom.adapters.results.BaseResultValue
import visdom.adapters.results.IdValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class MetadataResult[MetadataData <: Data](
    _id: String,
    id: String,
    `type`: String,
    name: String,
    description: String,
    origin: ItemLink,
    data: MetadataData,
    related_constructs: Seq[ItemLink],
    related_events: Seq[ItemLink]
)
extends BaseResultValue
with IdValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toBsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(`type`),
                SnakeCaseConstants.Name -> JsonUtils.toBsonValue(name),
                SnakeCaseConstants.Description -> JsonUtils.toBsonValue(description),
                SnakeCaseConstants.Origin -> origin.toBsonValue(),
                SnakeCaseConstants.Data -> data.toBsonValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toBsonValue(related_constructs.map(link => link.toBsonValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toBsonValue(related_events.map(link => link.toBsonValue()))
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(`type`),
                SnakeCaseConstants.Name -> JsonUtils.toJsonValue(name),
                SnakeCaseConstants.Description -> JsonUtils.toJsonValue(description),
                SnakeCaseConstants.Origin -> origin.toJsValue(),
                SnakeCaseConstants.Data -> data.toJsValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toJsonValue(related_constructs.map(link => link.toJsValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toJsonValue(related_events.map(link => link.toJsValue()))
            )
        )
    }
}

object MetadataResult {
    type CourseMetadataResult = MetadataResult[CourseData]
    type ModuleMetadataResult = MetadataResult[ModuleData]
    type ExerciseMetadataResult = MetadataResult[ExerciseData]

    def fromMetadata[MetadataData <: Data](
        metadata: Metadata,
        metadataData: MetadataData
    ): MetadataResult[MetadataData] = {
        MetadataResult(
            _id = metadata.id,
            id = metadata.id,
            `type` = metadata.getType,
            name = metadata.name,
            description = metadata.description,
            origin = metadata.origin,
            data = metadataData,
            related_constructs = metadata.relatedConstructs.map(link => ItemLink.fromLinkTrait(link)),
            related_events = metadata.relatedEvents.map(link => ItemLink.fromLinkTrait(link))
        )
    }

    def fromCourseSchema(courseSchema: CourseSchema): CourseMetadataResult = {
        val courseMetadata: CourseMetadata = new CourseMetadata(courseSchema)
        fromMetadata(courseMetadata, courseMetadata.data)
    }

    def fromModuleSchema(
        moduleSchema: ModuleSchema,
        moduleAdditionalSchema: ModuleAdditionalSchema
    ): ModuleMetadataResult = {
        val moduleMetadata: ModuleMetadata = new ModuleMetadata(moduleSchema, moduleAdditionalSchema)
        fromMetadata(moduleMetadata, moduleMetadata.data)
    }

    def fromExerciseSchema(exerciseSchema: ExerciseSchema): ExerciseMetadataResult = {
        val exerciseMetadata: ExerciseMetadata = new ExerciseMetadata(exerciseSchema)
        fromMetadata(exerciseMetadata, exerciseMetadata.data)
    }
}
