package visdom.adapters.options

import org.mongodb.scala.bson.BsonDocument
import visdom.http.server.options.LinkTypes
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class MultiQueryOptions(
    targetType: String,
    objectType: Option[String],
    query: Option[Seq[AttributeFilter]],
    dataAttributes: Option[Seq[String]],
    includedLinks: LinkTypes,
    page: Int,
    pageSize: Int
)
extends BaseQueryWithPageOptions {
    def toBsonDocument(): BsonDocument = {
        BsonDocument(
            Map(
                SnakeCaseConstants.TargetType -> JsonUtils.toBsonValue(targetType),
                SnakeCaseConstants.ObjectType -> JsonUtils.toBsonValue(objectType),
                SnakeCaseConstants.DataAttributes -> JsonUtils.toBsonValue(dataAttributes),
                SnakeCaseConstants.IncludedLinks -> JsonUtils.toBsonValue(includedLinks.linkType),
                SnakeCaseConstants.Page -> JsonUtils.toBsonValue(page),
                SnakeCaseConstants.PageSize -> JsonUtils.toBsonValue(pageSize)
            )
        )
    }
}
