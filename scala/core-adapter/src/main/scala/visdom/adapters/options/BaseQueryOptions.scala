package visdom.adapters.options

import org.mongodb.scala.bson.BsonDocument


abstract class BaseQueryOptions {
    def toBsonDocument(): BsonDocument
}
