package visdom.fetchers.gitlab

import java.time.Instant
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonElement
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonString
import scala.collection.JavaConverters.seqAsJavaListConverter
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.CommonConstants
import org.mongodb.scala.bson.collection.immutable.Document
import akka.http.scaladsl
import visdom.http.HttpUtils
import visdom.http.HttpConstants


class GitlabPipelineJobsHandler(options: GitlabPipelineOptions)
extends GitlabDataHandler(options) {
    def getFetcherType(): String = GitlabConstants.FetcherTypeJobs
    def getCollectionName(): String = MongoConstants.CollectionJobs

    override val createMetadataDocument: Boolean = false

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/jobs.html#list-pipeline-jobs
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathPipelines,
            options.pipelineId.toString(),
            GitlabConstants.PathJobs
        ).mkString(CommonConstants.Slash)

        options.hostServer.modifyRequest(Http(uri))
    }

    override def getIdentifierAttributes(): Array[String] = {
        Array(
            GitlabConstants.AttributeId,
            GitlabConstants.AttributeProjectName,
            GitlabConstants.AttributeHostName
        )
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        addIdentifierAttributes(document).append(
            GitlabConstants.AttributeMetadata, getMetadata()
        )
    }

    private def addIdentifierAttributes(document: BsonDocument): BsonDocument = {
        document
            .append(GitlabConstants.AttributeProjectName, new BsonString(options.projectName))
            .append(GitlabConstants.AttributeHostName, new BsonString(options.hostServer.hostName))
    }

    private def getMetadata(): BsonDocument = {
        new BsonDocument(
            List(
                new BsonElement(
                    GitlabConstants.AttributeLastModified,
                    new BsonDateTime(Instant.now().toEpochMilli())
                ),
                new BsonElement(
                    GitlabConstants.AttributeApiVersion,
                    new BsonInt32(GitlabConstants.GitlabApiVersion)
                )
            ).asJava
        )
    }
}
