package visdom.adapters.options

import org.mongodb.scala.bson.BsonDocument
import visdom.utils.SnakeCaseConstants
import visdom.json.JsonUtils


final case class TestQueryOptions(
    page: Int,
    pageSize: Int,
    target: TestTargetType,
    token: Option[String]
)
extends BaseQueryWithPageOptions {
    def toBsonDocument(): BsonDocument = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Page -> JsonUtils.toBsonValue(page),
                SnakeCaseConstants.PageSize -> JsonUtils.toBsonValue(pageSize),
                SnakeCaseConstants.Target -> JsonUtils.toBsonValue(target.targetType),
                SnakeCaseConstants.Token -> JsonUtils.toBsonValue(token)
            )
        )
    }
}
