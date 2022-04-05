package visdom.adapters.dataset.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ProjectCommitStats(
    files: Int,
    additions: Int,
    deletions: Int
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Files -> JsonUtils.toBsonValue(files),
                SnakeCaseConstants.Additions -> JsonUtils.toBsonValue(additions),
                SnakeCaseConstants.Deletions -> JsonUtils.toBsonValue(deletions)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Files -> JsonUtils.toJsonValue(files),
                SnakeCaseConstants.Additions -> JsonUtils.toJsonValue(additions),
                SnakeCaseConstants.Deletions -> JsonUtils.toJsonValue(deletions)
            )
        )
    }
}
