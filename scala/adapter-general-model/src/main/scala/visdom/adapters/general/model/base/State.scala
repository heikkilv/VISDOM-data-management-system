package visdom.adapters.general.model.base

import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.BsonValue
import spray.json.JsString
import spray.json.JsValue
import visdom.adapters.results.BaseResultValue


abstract class State
extends BaseResultValue {
    val stateString: String

    def toBsonValue(): BsonValue = {
        BsonString(stateString)
    }

    def toJsValue(): JsValue = {
        JsString(stateString)
    }
}
