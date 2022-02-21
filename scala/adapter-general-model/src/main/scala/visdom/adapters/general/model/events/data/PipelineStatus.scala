package visdom.adapters.general.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.schemas.PipelineSchema
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class PipelineStatus(
    text: String,
    label: String,
    group: String
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Text -> JsonUtils.toBsonValue(text),
                SnakeCaseConstants.Label -> JsonUtils.toBsonValue(label),
                SnakeCaseConstants.Group -> JsonUtils.toBsonValue(group)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Text -> JsonUtils.toJsonValue(text),
                SnakeCaseConstants.Label -> JsonUtils.toJsonValue(label),
                SnakeCaseConstants.Group -> JsonUtils.toJsonValue(group)
            )
        )
    }
}

object PipelineStatus {
    def fromCommitStatsSchema(pipelineSchema: PipelineSchema): PipelineStatus = {
        PipelineStatus(
            text = pipelineSchema.detailed_status.text,
            label = pipelineSchema.detailed_status.label,
            group = pipelineSchema.detailed_status.group
        )
    }
}
