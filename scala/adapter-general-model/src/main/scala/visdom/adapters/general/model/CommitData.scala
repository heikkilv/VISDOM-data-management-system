package visdom.adapters.general.model

import org.bson.BsonValue
import org.mongodb.scala.bson.BsonDocument
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class CommitData(
    commit_id: String
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.CommitId -> JsonUtils.toBsonValue(commit_id)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.CommitId -> JsonUtils.toJsonValue(commit_id)
            )
        )
    }
}
