package visdom.adapters.options

import org.mongodb.scala.bson.BsonDocument
import visdom.utils.SnakeCaseConstants
import visdom.json.JsonUtils


final case class QueryWithOnlyPageOptions(
    page: Int,
    pageSize: Int
)
extends BaseQueryWithPageOptions {
    def toBsonDocument(): BsonDocument = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Page -> JsonUtils.toBsonValue(page),
                SnakeCaseConstants.PageSize -> JsonUtils.toBsonValue(pageSize)
            )
        )
    }
}
