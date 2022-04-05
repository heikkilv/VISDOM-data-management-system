package visdom.adapters.dataset.model.artifacts.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class SonarRatingsData(
    reliability: Int,
    security: Int
)

extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Reliability -> JsonUtils.toBsonValue(reliability),
                SnakeCaseConstants.Security -> JsonUtils.toBsonValue(security)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Reliability -> JsonUtils.toJsonValue(reliability),
                SnakeCaseConstants.Security -> JsonUtils.toJsonValue(security)
            )
        )
    }
}
