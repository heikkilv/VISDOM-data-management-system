package visdom.adapters.general.model.base

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsValue
import spray.json.JsObject
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ItemLink(
    id: String,
    linkType: String
)
extends LinkTrait
with BaseResultValue {
    def getType: String = linkType

    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toBsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(linkType)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(linkType)
            )
        )
    }
}

object ItemLink {
    def fromLinkTrait(objectWithLinkTrait: LinkTrait): ItemLink = {
        ItemLink(
            id = objectWithLinkTrait.id,
            linkType = objectWithLinkTrait.getType
        )
    }
}
