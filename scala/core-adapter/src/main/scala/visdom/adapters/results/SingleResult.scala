package visdom.adapters.results

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.json.JsonUtils


case class SingleResult(
    result: JsObject
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(result.fields.mapValues(value => JsonUtils.toBsonValue(value)))
    }

    def toJsValue(): JsValue = {
        result
    }
}
