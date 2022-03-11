package visdom.adapters.general.model.origins.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class  AplusOriginData(
    code: Option[String]
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(Map(SnakeCaseConstants.Code -> JsonUtils.toBsonValue(code)))
    }

    def toJsValue(): JsValue = {
        JsObject(Map(SnakeCaseConstants.Code -> JsonUtils.toJsonValue(code)))
    }
}
