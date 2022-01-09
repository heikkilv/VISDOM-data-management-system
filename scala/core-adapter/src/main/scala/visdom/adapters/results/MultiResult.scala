package visdom.adapters.results

import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.BsonValue
import spray.json.JsArray
import spray.json.JsValue
import visdom.json.JsonUtils



case class MultiResult(
    results: Seq[JsValue]
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonArray.fromIterable(results.map(result => JsonUtils.toBsonValue(result)))
    }

    def toJsValue(): JsValue = {
        JsArray(results:_*)
    }
}
