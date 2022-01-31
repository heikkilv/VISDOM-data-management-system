package visdom.adapters.general.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class CommitRef(
    refType: String,
    name: String
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(refType),
                SnakeCaseConstants.Name -> JsonUtils.toBsonValue(name)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(refType),
                SnakeCaseConstants.Name -> JsonUtils.toJsonValue(name)
            )
        )
    }
}
