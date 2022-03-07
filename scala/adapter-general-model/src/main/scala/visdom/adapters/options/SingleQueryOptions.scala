package visdom.adapters.options

import org.mongodb.scala.bson.BsonDocument
import visdom.utils.SnakeCaseConstants
import visdom.json.JsonUtils


final case class SingleQueryOptions(
    objectType: String,
    uuid: String
)
extends BaseQueryOptions {
    def toBsonDocument(): BsonDocument = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(objectType),
                SnakeCaseConstants.Uuid -> JsonUtils.toBsonValue(uuid)
            )
        )
    }
}
