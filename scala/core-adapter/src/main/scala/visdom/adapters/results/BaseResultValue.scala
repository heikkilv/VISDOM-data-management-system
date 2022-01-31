package visdom.adapters.results

import org.mongodb.scala.bson.BsonValue
import spray.json.JsValue


trait BaseResultValue {
    def toBsonValue(): BsonValue
    def toJsValue(): JsValue
}
