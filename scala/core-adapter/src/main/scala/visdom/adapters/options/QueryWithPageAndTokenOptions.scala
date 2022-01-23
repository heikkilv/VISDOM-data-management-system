package visdom.adapters.options

import org.mongodb.scala.bson.BsonDocument
import visdom.utils.SnakeCaseConstants
import visdom.json.JsonUtils


final case class QueryWithPageAndTokenOptions(
    page: Int,
    pageSize: Int,
    token: Option[String]
)
extends BaseQueryWithPageOptions {
    def toBsonDocument(): BsonDocument = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Page -> JsonUtils.toBsonValue(page),
                SnakeCaseConstants.PageSize -> JsonUtils.toBsonValue(pageSize),
                SnakeCaseConstants.Token -> JsonUtils.toBsonValue(token)
            )
        )
    }
}
