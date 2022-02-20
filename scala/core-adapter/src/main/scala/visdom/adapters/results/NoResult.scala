package visdom.adapters.results

import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.BsonValue
import spray.json.JsValue
import spray.json.JsString


final case class NoResult(
    message: String
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonString(message)
    }

    def toJsValue(): JsValue = {
        JsString(message)
    }
}
