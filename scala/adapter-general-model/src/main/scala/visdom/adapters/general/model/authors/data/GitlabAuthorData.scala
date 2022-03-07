package visdom.adapters.general.model.authors.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class GitlabAuthorData(
    user_id: Int,
    username: String
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.UserId -> JsonUtils.toBsonValue(user_id),
                SnakeCaseConstants.Username -> JsonUtils.toBsonValue(username)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.UserId -> JsonUtils.toJsonValue(user_id),
                SnakeCaseConstants.Username -> JsonUtils.toJsonValue(username)
            )
        )
    }
}
