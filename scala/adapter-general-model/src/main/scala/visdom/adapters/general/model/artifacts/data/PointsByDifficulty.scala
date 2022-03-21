package visdom.adapters.general.model.artifacts.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsValue
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.PascalCaseConstants


final case class PointsByDifficulty(
    categoryN: Option[Int],
    categoryP: Option[Int],
    categoryG: Option[Int]
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument()
            .appendOption(PascalCaseConstants.N, categoryN.map(value => JsonUtils.toBsonValue(value)))
            .appendOption(PascalCaseConstants.P, categoryP.map(value => JsonUtils.toBsonValue(value)))
            .appendOption(PascalCaseConstants.G, categoryG.map(value => JsonUtils.toBsonValue(value)))
    }

    def toJsValue(): JsValue = {
        JsonUtils.toJsonValue(toBsonValue())
    }
}
