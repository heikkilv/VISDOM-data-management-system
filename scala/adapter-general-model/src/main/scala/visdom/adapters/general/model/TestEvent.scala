package visdom.adapters.general.model

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants


final case class TestEvent(
    id: String,
    event_type: String,
    time: String,
    duration: Double,
    message: String,
    data: CommitData
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toBsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(event_type),
                SnakeCaseConstants.Time -> JsonUtils.toBsonValue(time),
                SnakeCaseConstants.Duration -> JsonUtils.toBsonValue(duration),
                SnakeCaseConstants.Message -> JsonUtils.toBsonValue(message),
                SnakeCaseConstants.Data -> data.toBsonValue()
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(event_type),
                SnakeCaseConstants.Time -> JsonUtils.toJsonValue(time),
                SnakeCaseConstants.Duration -> JsonUtils.toJsonValue(duration),
                SnakeCaseConstants.Message -> JsonUtils.toJsonValue(message),
                SnakeCaseConstants.Data -> data.toJsValue()
            )
        )
    }
}

object TestEvent {
    def fromCommitSchema(commitSchema: CommitSchema): TestEvent = {
        TestEvent(
            id = GeneralUtils.getUuid(
                commitSchema.host_name,
                commitSchema.project_name,
                commitSchema.id
            ),
            event_type = SnakeCaseConstants.Commit,
            time = commitSchema.committed_date,
            duration = 0.0,
            message = commitSchema.message,
            data = CommitData(
                commit_id = commitSchema.id
            )
        )
    }
}
