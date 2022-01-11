package visdom.adapters.general.model

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class TestEvent(
    id: String,
    message: String,
    timestamp: String
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toBsonValue(id),
                SnakeCaseConstants.Message -> JsonUtils.toBsonValue(message),
                SnakeCaseConstants.Timestamp -> JsonUtils.toBsonValue(timestamp)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
                SnakeCaseConstants.Message -> JsonUtils.toJsonValue(message),
                SnakeCaseConstants.Timestamp -> JsonUtils.toJsonValue(timestamp)
            )
        )
    }
}

object TestEvent {
    def fromCommitSchema(commitSchema: CommitSchema): TestEvent = {
        TestEvent(
            id = commitSchema.id,
            message = commitSchema.message,
            timestamp = commitSchema.committed_date
        )
    }
}
