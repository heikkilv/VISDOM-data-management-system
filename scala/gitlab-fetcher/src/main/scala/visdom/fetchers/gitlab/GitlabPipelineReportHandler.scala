package visdom.fetchers.gitlab

import java.time.Instant
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonElement
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.Document
import scala.collection.JavaConverters.seqAsJavaListConverter
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConstants
import visdom.utils.CommonConstants


class GitlabPipelineReportHandler(options: GitlabPipelineReportOptions)
extends GitlabDataHandler(options) {

    def getFetcherType(): String = GitlabConstants.FetcherTypePipelineReport
    def getCollectionName(): String = MongoConstants.CollectionPipelineReports

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            GitlabConstants.AttributeUseAnonymization -> options.useAnonymization
        )
    }

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/pipelines.html#get-a-pipelines-test-report
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathPipelines,
            options.pipelineId.toString,
            GitlabConstants.PathTestReport
        ).mkString(CommonConstants.Slash)

        options.hostServer.modifyRequest(Http(uri))
    }

    override def getIdentifierAttributes(): Array[String] = {
        Array(
            GitlabConstants.AttributePipelineId,
            GitlabConstants.AttributeProjectName,
            GitlabConstants.AttributeHostName
        )
    }

    override def getHashableAttributes(): Option[Seq[Seq[String]]] = {
        options.useAnonymization match {
            case true => Some(Seq(Seq(GitlabConstants.AttributeProjectName)))
            case false => None
        }
    }

    override def processResponse(response: HttpResponse[String]): Array[Document] = {
        // the pipeline test report is only one JSON object while the process expect an array of objects
        // the following is a hack to delay the required refactoring to the future
        super.processResponse(
            HttpResponse(
                CommonConstants.SquareBracketBegin + response.body + CommonConstants.SquareBracketEnd,
                response.code,
                response.headers
            )
        )
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        addIdentifierAttributes(document)
            .append(GitlabConstants.AttributeMetadata, getMetadata())
    }

    private def addIdentifierAttributes(document: BsonDocument): BsonDocument = {
        document
            .append(GitlabConstants.AttributePipelineId, new BsonInt32(options.pipelineId))
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
