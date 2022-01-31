package visdom.adapters.general.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class CommitStats(
    additions: Int,
    deletions: Int,
    total: Int
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Additions -> JsonUtils.toBsonValue(additions),
                SnakeCaseConstants.Deletions -> JsonUtils.toBsonValue(deletions),
                SnakeCaseConstants.Total -> JsonUtils.toBsonValue(total)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Additions -> JsonUtils.toJsonValue(additions),
                SnakeCaseConstants.Deletions -> JsonUtils.toJsonValue(deletions),
                SnakeCaseConstants.Total -> JsonUtils.toJsonValue(total)
            )
        )
    }
}