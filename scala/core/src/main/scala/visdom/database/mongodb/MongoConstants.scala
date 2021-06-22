package visdom.database.mongodb

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

object MongoConstants {
    val ApplicationName: String = "APPLICATION_NAME"
    val MongoHost: String = "MONGODB_HOST"
    val MongoPort: String = "MONGODB_PORT"
    val MongoUserName: String = "MONGODB_USERNAME"
    val MongoPassword: String = "MONGODB_PASSWORD"
    val MongoMetadataDatabase: String = "MONGODB_METADATA_DATABASE"
    val MongoTargetDatabase: String = "MONGO_DATA_DATABASE"

    val DefaultApplicationName: String = "gitlab-fetcher"
    val DefaultMongoHost: String = "localhost"
    val DefaultMongoPort: Int = 27017
    val DefaultMongoUserName: String = ""
    val DefaultMongoPassword: String = ""
    val DefaultMongoMetadataDatabase: String = "metadata"
    val DefaultMongoTargetDatabase: String = "gitlab"

    val CollectionCommits: String = "commits"
    val CollectionFiles: String = "files"
    val CollectionMetadata: String = "metadata"
    val CollectionTemp: String = "temp"

    val AttributeDefaultId: String = "_id"

    // the default maximum delay until a MongoDB query will be considered failed
    val DefaultMaxQueryDelaySeconds: Int = 15
    val DefaultMaxQueryDelay: Duration = Duration(DefaultMaxQueryDelaySeconds, TimeUnit.SECONDS)

}
