package visdom.adapters.results

import org.mongodb.scala.bson.BsonValue
import spray.json.JsValue


abstract class BaseResultValue {
    def toBsonValue(): BsonValue
    def toJsValue(): JsValue
}
