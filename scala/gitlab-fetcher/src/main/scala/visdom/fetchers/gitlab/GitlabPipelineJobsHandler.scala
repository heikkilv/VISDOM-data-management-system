package visdom.fetchers.gitlab

import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.BsonBoolean
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.Document
import scalaj.http.Http
import visdom.http.HttpConstants
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import visdom.http.HttpUtils
import visdom.database.mongodb.MongoConnection
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonValue
import visdom.utils.CommonConstants


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

    override def getHashableAttributes(): Option[Seq[Seq[String]]] = {
        options.useAnonymization match {
            case true => Some(
                Seq(
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeId),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeName),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeUsername),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeAvatarUrl),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeWebUrl),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeBio),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeBioHtml),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeLocation),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributePublicEmail),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeSkype),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeLinkedin),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeTwitter),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeWebsiteUrl),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeOrganization),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeJobTitle),
                    Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeWorkInformation),
                    Seq(GitlabConstants.AttributeCommit, GitlabConstants.AttributeAuthorName),
                    Seq(GitlabConstants.AttributeCommit, GitlabConstants.AttributeAuthorEmail),
                    Seq(GitlabConstants.AttributeCommit, GitlabConstants.AttributeCommitterName),
                    Seq(GitlabConstants.AttributeCommit, GitlabConstants.AttributeCommitterEmail),
                    Seq(GitlabConstants.AttributeCommit, GitlabConstants.AttributeWebUrl),
                    Seq(GitlabConstants.AttributePipeline, GitlabConstants.AttributeWebUrl),
                    Seq(GitlabConstants.AttributeWebUrl),
                    Seq(GitlabConstants.AttributeProjectName)
                )
            )
            case false => None
        }
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        val jobLogFetched: Boolean = options.includeJobLogs match {
            case true => document.getIntOption(GitlabConstants.AttributeId) match {
                case Some(jobId: Int) => fetchJobLog(jobId)
                case None => false
            }
            case false => false
        }

        addIdentifierAttributes(document)
            .append(GitlabConstants.AttributeJobLogIncluded, BsonBoolean(jobLogFetched))
            .append(GitlabConstants.AttributeMetadata, getMetadata())
    }

    private def getMetadata(): BsonDocument = {
        getMetadataBase()
            .append(GitlabConstants.AttributeUseAnonymization, toBsonValue(options.useAnonymization))
    }

    private def fetchJobLog(jobId: Int): Boolean = {
        val jobLogUri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathJobs,
            jobId.toString(),
            GitlabConstants.PathTrace
        ).mkString(CommonConstants.Slash)
        val jobLogRequest: HttpRequest = options.hostServer.modifyRequest(Http(jobLogUri))

        val logOption: Option[String] = HttpUtils.getRequestBody(jobLogRequest, HttpConstants.StatusCodeOk)
        logOption match {
            case Some(log: String) => {
                saveJobLog(getJobLogDocument(jobId, log))
                true
            }
            case None => false
        }
    }

    private def getJobLogDocument(jobId: Int, log: String): BsonDocument = {
        addIdentifierAttributes(
            BsonDocument(
                Map(
                    GitlabConstants.AttributeId -> BsonInt32(jobId),
                    GitlabConstants.AttributeLog -> BsonString(log)
                )
            )
        )
            .append(GitlabConstants.AttributeMetadata, getMetadata())
            .anonymize(getHashableAttributes())
    }

    private def saveJobLog(jobLogBsonDocument: BsonDocument): Unit = {
        val jobLogCollection: Option[MongoCollection[Document]] = options.mongoDatabase match {
            case Some(database: MongoDatabase) =>
                Some(database.getCollection(MongoConstants.CollectionJobLogs))
            case None => None
        }

        jobLogCollection match {
            case Some(collection: MongoCollection[Document]) => MongoConnection.storeDocument(
                collection = collection,
                document = Document(jobLogBsonDocument),
                identifierAttributes = Array(
                    GitlabConstants.AttributeId,
                    GitlabConstants.AttributeProjectName,
                    GitlabConstants.AttributeHostName
                )
            )
            case None =>
        }
    }
}
