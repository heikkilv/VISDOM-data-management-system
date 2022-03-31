package visdom.adapters.dataset.model.authors.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data


final case class UserData()
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument()
    }

    def toJsValue(): JsValue = {
        JsObject()
    }
}
