package visdom.fetchers.gitlab

import java.time.Instant
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.Document
import visdom.database.mongodb.MongoConnection.applicationName
import visdom.database.mongodb.MongoConnection.mongoClient
import visdom.database.mongodb.MongoConnection.storeDocument
import visdom.database.mongodb.MongoConstants


object Routes {
    private val metadataDatabaseName: String = sys.env.getOrElse(
        MongoConstants.MongoMetadataDatabase,
        MongoConstants.DefaultMongoMetadataDatabase
    )
    val databaseName: String = sys.env.getOrElse(
        MongoConstants.MongoTargetDatabase,
        MongoConstants.DefaultMongoTargetDatabase
    )

    val server: GitlabServer = new GitlabServer(
        hostAddress = sys.env.getOrElse(
            GitlabConstants.EnvironmentGitlabHost,
            GitlabConstants.DefaultGitlabHost
        ),
        apiToken = Some(
            sys.env.getOrElse(
                GitlabConstants.EnvironmentGitlabToken,
                GitlabConstants.DefaultGitlabToken
            )
        ),
        allowUnsafeSSL = Some(
            sys.env.getOrElse(
                GitlabConstants.EnvironmentGitlabInsecure,
                GitlabConstants.DefaultGitlabInsecure
            ).toBoolean
        )
    )
    private val metadataDatabase: MongoDatabase = mongoClient.getDatabase(metadataDatabaseName)
    val targetDatabase: MongoDatabase = mongoClient.getDatabase(databaseName)

    private def handleData(
        fetchOptions: GitlabFetchOptions
    ): Int = {
        val dataHandlerOption: Option[GitlabDataHandler] = fetchOptions match {
            case commitFetchOption: GitlabCommitOptions => Some(new GitlabCommitHandler(commitFetchOption))
            case fileFetcherOptions: GitlabFileOptions => Some(new GitlabFileHandler(fileFetcherOptions))
            case _ => None
        }
        val documentsOption: Option[Array[Document]] = dataHandlerOption match {
            case Some(dataHandler: GitlabDataHandler) => dataHandler.process()
            case None => None
        }
        documentsOption match {
            case Some(documents: Array[Document]) => documents.size
            case None => 0
        }
    }

    def storeMetadata(): Unit = {
        val metadataDocument: Document = Document(
            BsonDocument(
                GitlabConstants.AttributeApplicationName -> applicationName,
                GitlabConstants.AttributeType -> GitlabConstants.FetcherType,
                GitlabConstants.AttributeVersion -> GitlabConstants.FetcherVersion,
                GitlabConstants.AttributeDatabase -> databaseName
            )
            .append(
                GitlabConstants.AttributeTimestamp,
                BsonDateTime(Instant.now().toEpochMilli())
            )
        )
        storeDocument(
            metadataDatabase.getCollection(MongoConstants.CollectionMetadata),
            metadataDocument,
            Array(
                GitlabConstants.AttributeApplicationName,
                GitlabConstants.AttributeType,
                GitlabConstants.AttributeVersion
            )
        )
    }
}
