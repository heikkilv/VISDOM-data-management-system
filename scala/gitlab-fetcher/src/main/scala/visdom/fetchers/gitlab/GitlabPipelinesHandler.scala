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
import visdom.http.HttpUtils
import visdom.http.HttpConstants


class GitlabPipelinesHandler(options: GitlabPipelinesOptions)
extends GitlabDataHandler(options) {
    def getFetcherType(): String = GitlabConstants.FetcherTypePipelines
    def getCollectionName(): String = MongoConstants.CollectionPipelines

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/pipelines.html#list-project-pipelines
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathPipelines
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
        val detailedDocument: BsonDocument = document.getIntOption(GitlabConstants.AttributeId) match {
            case Some(pipelineId: Int) => {
                fetchSinglePipelineData(pipelineId) match {
                    case Some(pipelineDocument: BsonDocument) => {
                        val a = fetchJobData(pipelineId)
                        pipelineDocument
                    }
                    case None => {
                        println(s"Failed to fetch detailed pipeline document for pipeline ${pipelineId}")
                        document
                    }
                }
            }
            case None => {
                println("Did not find pipeline id from the pipeline document")
                document
            }
        }

        addIdentifierAttributes(detailedDocument).append(
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

    private def fetchSinglePipelineData(pipelineId: Int): Option[BsonDocument] = {
        val singlePipelineUri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathPipelines,
            pipelineId.toString()
        ).mkString(CommonConstants.Slash)
        val singlePipelineRequest: HttpRequest = options.hostServer.modifyRequest(Http(singlePipelineUri))

        HttpUtils.getRequestDocument(singlePipelineRequest, HttpConstants.StatusCodeOk)
    }


    private def fetchJobData(pipelineId: Int): Option[Array[Document]] = {
        val jobsOptions: GitlabPipelineOptions = GitlabPipelineOptions(
            hostServer = options.hostServer,
            mongoDatabase = options.mongoDatabase,
            projectName = options.projectName,
            pipelineId = pipelineId
        )

        (new GitlabPipelineJobsHandler(jobsOptions)).process()
    }
}
