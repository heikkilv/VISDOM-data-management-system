package visdom.fetchers.gitlab

import java.time.Instant
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.BsonBoolean
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonElement
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.Document
import scala.collection.JavaConverters.seqAsJavaListConverter
import scalaj.http.Http
import visdom.http.HttpConstants
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import visdom.http.HttpUtils
import visdom.database.mongodb.MongoConnection
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils.EnrichedBsonDocument
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
                    GitlabConstants.AttributeJobId -> BsonInt32(jobId),
                    GitlabConstants.AttributeLog -> BsonString(log)
                )
            )
        ).append(GitlabConstants.AttributeMetadata, getMetadata())

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
                    GitlabConstants.AttributeJobId,
                    GitlabConstants.AttributeProjectName,
                    GitlabConstants.AttributeHostName
                )
            )
            case None =>
        }
    }
}
