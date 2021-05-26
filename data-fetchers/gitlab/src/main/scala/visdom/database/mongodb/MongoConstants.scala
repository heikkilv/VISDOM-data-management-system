package visdom.database.mongodb

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
}
