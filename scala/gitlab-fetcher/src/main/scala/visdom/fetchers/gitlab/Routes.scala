package visdom.fetchers.gitlab

import java.time.Instant
import java.util.Timer
import java.util.TimerTask
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.Document
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import visdom.database.mongodb.MongoConnection.applicationName
import visdom.database.mongodb.MongoConnection.mongoClient
import visdom.database.mongodb.MongoConnection.storeDocument
import visdom.database.mongodb.MongoConstants


object Routes {
    implicit val ec: ExecutionContext = ExecutionContext.global

    val AttributeApiAddress: String = "api_address"
    val AttributeStartTime: String = "start_time"
    val AttributeSwaggerDefinition: String = "swagger_definition"

    final val SwaggerLocation: String = "/api-docs/swagger.json"


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
                GitlabConstants.AttributeComponentType -> GitlabConstants.ComponentType,
                GitlabConstants.AttributeFetcherType -> GitlabConstants.FetcherType,
                GitlabConstants.AttributeVersion -> GitlabConstants.FetcherVersion,
                GitlabConstants.AttributeGitlabServer -> server.hostName,
                GitlabConstants.AttributeDatabase -> databaseName,
                AttributeApiAddress -> SwaggerFetcherDocService.host,
                AttributeSwaggerDefinition -> SwaggerLocation,
                AttributeStartTime -> GitlabFetcher.StartTime
            )
            .append(GitlabConstants.AttributeTimestamp, BsonDateTime(Instant.now().toEpochMilli()))
        )
        storeDocument(
            metadataDatabase.getCollection(MongoConstants.CollectionMetadata),
            metadataDocument,
            Array(
                GitlabConstants.AttributeApplicationName,
                GitlabConstants.AttributeComponentType,
                GitlabConstants.AttributeFetcherType,
                GitlabConstants.AttributeVersion
            )
        )
    }

    val MetadataInitialDelay: Long = 0
    val MetadataUpdateInterval: Long = 300000

    val metadataTimer: Timer = new Timer()

    val metadataTask: TimerTask = new TimerTask {
        def run() = {
            val metadataTask: Future[Unit] = Future(storeMetadata())
        }
    }

    def startMetadataTask(): Unit = {
        metadataTimer.schedule(metadataTask, MetadataInitialDelay, MetadataUpdateInterval)
    }

    def stopMetadataTask(): Unit = {
        val _ = metadataTask.cancel()
    }
}
