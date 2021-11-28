package visdom.fetchers.gitlab

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.Document
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConstants
import visdom.http.HttpUtils
import visdom.utils.CommonConstants


class GitlabPipelineReportHandler(options: GitlabPipelineReportOptions)
extends GitlabDataHandler(options) {

    def getFetcherType(): String = GitlabConstants.FetcherTypePipelineReport
    def getCollectionName(): String = MongoConstants.CollectionPipelineReports
    override val createMetadataDocument: Boolean = false

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
            case true => Some(
                Seq(
                    Seq(GitlabConstants.AttributeProjectName)
                )
            )
            case false => None
        }
    }

    override def responseToDocumentArray(response: HttpResponse[String]): Array[BsonDocument] = {
        // the pipeline test report is only one JSON object while the default process expects an array of objects
        HttpUtils.responseToDocumentArrayCaseDocument(response)
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        addIdentifierAttributes(document)
            .append(GitlabConstants.AttributeMetadata, getMetadata())
    }

    override protected def addIdentifierAttributes(document: BsonDocument): BsonDocument = {
        super.addIdentifierAttributes(document)
            .append(GitlabConstants.AttributePipelineId, new BsonInt32(options.pipelineId))
    }

    private def getMetadata(): BsonDocument = {
        getMetadataBase()
    }
}
