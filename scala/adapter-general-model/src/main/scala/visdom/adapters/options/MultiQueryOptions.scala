package visdom.adapters.options

import org.mongodb.scala.bson.BsonDocument
import visdom.utils.SnakeCaseConstants
import visdom.json.JsonUtils


final case class MultiQueryOptions(
    targetType: String,
    objectType: Option[String],
    dataAttributes: Option[Seq[String]],
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
                SnakeCaseConstants.Page -> JsonUtils.toBsonValue(page),
                SnakeCaseConstants.PageSize -> JsonUtils.toBsonValue(pageSize)
            )
        )
    }
}
