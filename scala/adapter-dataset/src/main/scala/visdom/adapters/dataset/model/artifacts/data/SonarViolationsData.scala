package visdom.adapters.dataset.model.artifacts.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class SonarViolationsData(
    blocker: Int,
    critical: Int,
    major: Int,
    minor: Int,
    info: Int,
    total: Int
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Blocker -> JsonUtils.toBsonValue(blocker),
                SnakeCaseConstants.Critical -> JsonUtils.toBsonValue(critical),
                SnakeCaseConstants.Major -> JsonUtils.toBsonValue(major),
                SnakeCaseConstants.Minor -> JsonUtils.toBsonValue(minor),
                SnakeCaseConstants.Info -> JsonUtils.toBsonValue(info),
                SnakeCaseConstants.Total -> JsonUtils.toBsonValue(total)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Blocker -> JsonUtils.toJsonValue(blocker),
                SnakeCaseConstants.Critical -> JsonUtils.toJsonValue(critical),
                SnakeCaseConstants.Major -> JsonUtils.toJsonValue(major),
                SnakeCaseConstants.Minor -> JsonUtils.toJsonValue(minor),
                SnakeCaseConstants.Info -> JsonUtils.toJsonValue(info),
                SnakeCaseConstants.Total -> JsonUtils.toJsonValue(total)
            )
        )
    }
}
