// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
import visdom.fetchers.FetcherList
import visdom.http.HttpConstants
import visdom.http.server.ServerConstants
import visdom.utils.CommonConstants
import visdom.utils.EnvironmentVariables.getEnvironmentVariable
import visdom.utils.EnvironmentVariables.EnvironmentApplicationName


object Routes {
    implicit val ec: ExecutionContext = ExecutionContext.global

    val AttributeApiAddress: String = "api_address"
    val AttributeStartTime: String = "start_time"
    val AttributeSwaggerDefinition: String = "swagger_definition"

    val fullApiAddress: String =
        HttpConstants.HttpPrefix.concat(
            SwaggerFetcherDocService.host.contains(HttpConstants.Localhost) match {
                case true => Seq(
                    getEnvironmentVariable(EnvironmentApplicationName),
                    ServerConstants.HttpInternalPort.toString()
                ).mkString(CommonConstants.DoubleDot)
                case false => SwaggerFetcherDocService.host
            }
        )
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

    def storeMetadata(): Unit = {
        val metadataDocument: Document = Document(
            BsonDocument(
                GitlabConstants.AttributeApplicationName -> applicationName,
                GitlabConstants.AttributeComponentType -> GitlabConstants.ComponentType,
                GitlabConstants.AttributeFetcherType -> GitlabConstants.FetcherType,
                GitlabConstants.AttributeVersion -> GitlabConstants.FetcherVersion,
                GitlabConstants.AttributeSourceServer -> server.hostName,
                GitlabConstants.AttributeDatabase -> databaseName,
                AttributeApiAddress -> fullApiAddress,
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

    val fetcherList: FetcherList = new FetcherList()
}
