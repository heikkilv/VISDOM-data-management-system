package visdom.fetchers.gitlab

import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit.SECONDS
import org.bson.BsonValue
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.BsonBoolean
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonElement
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.collection.immutable.Document
import scala.collection.JavaConverters.seqAsJavaListConverter
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import visdom.database.mongodb.MongoConstants
import visdom.http.HttpUtils
import visdom.http.HttpConstants
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonValue
import visdom.utils.CommonConstants
import visdom.utils.WartRemoverConstants


class GitlabPipelinesHandler(options: GitlabPipelinesOptions)
extends GitlabDataHandler(options) {
    def getFetcherType(): String = GitlabConstants.FetcherTypePipelines
    def getCollectionName(): String = MongoConstants.CollectionPipelines

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            GitlabConstants.AttributeReference -> options.reference,
            GitlabConstants.AttributeIncludeJobs -> options.includeJobs,
            GitlabConstants.AttributeIncludeJobLogs -> options.includeJobLogs
        )
        .appendOption(
            GitlabConstants.AttributeStartDate,
            options.startDate.map(dateValue => toBsonValue(dateValue))
        )
        .appendOption(
            GitlabConstants.AttributeEndDate,
            options.endDate.map(dateValue => toBsonValue(dateValue))
        )
    }

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/pipelines.html#list-project-pipelines
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathPipelines
        ).mkString(CommonConstants.Slash)

        options.hostServer.modifyRequest(
            processOptionalParameters(
                Http(uri)
                    .param(GitlabConstants.ParamRef, options.reference)
            )
        )
    }

    override def getIdentifierAttributes(): Array[String] = {
        Array(
            GitlabConstants.AttributeId,
            GitlabConstants.AttributeProjectName,
            GitlabConstants.AttributeHostName
        )
    }

    override def getHashableAttributes(): Option[Seq[Seq[String]]] = {
        Some(
            Seq(
                Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeName),
                Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeUserName),
                Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeAvatarUrl),
                Seq(GitlabConstants.AttributeUser, GitlabConstants.AttributeWebUrl)
            )
        )
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        val detailedDocument: BsonDocument = document.getIntOption(GitlabConstants.AttributeId) match {
            case Some(pipelineId: Int) => {
                fetchSinglePipelineData(pipelineId) match {
                    case Some(pipelineDocument: BsonDocument) => {
                        addJobData(pipelineDocument, pipelineId)
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
                ),
                new BsonElement(
                    GitlabConstants.AttributeIncludeJobs,
                    new BsonBoolean(options.includeJobs)
                ),
                new BsonElement(
                    GitlabConstants.AttributeIncludeJobLogs,
                    new BsonBoolean(options.includeJobLogs)
                )
            ).asJava
        )
    }

    private def processOptionalParameters(request: HttpRequest): HttpRequest = {
        @SuppressWarnings(Array(WartRemoverConstants.WartsVar))
        var paramMap: Seq[(String, String)] = Seq.empty

        options.startDate match {
            case Some(startDate: ZonedDateTime) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamUpdatedAfter,
                    startDate.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(SECONDS).toString()
                ))
            }
            case None =>
        }

        options.endDate match {
            case Some(endDate: ZonedDateTime) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamUpdatedBefore,
                    endDate.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(SECONDS).toString()
                ))
            }
            case None =>
        }

        request.params(paramMap)
    }

    private def fetchSinglePipelineData(pipelineId: Int): Option[BsonDocument] = {
        // https://docs.gitlab.com/ee/api/pipelines.html#get-a-single-pipeline
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

    private def getJobIds(pipelineJobs: Option[Array[Document]]): Option[Array[BsonInt32]] = {
        pipelineJobs match {
            case Some(jobs: Array[Document]) => {
                Some(
                    jobs.map(jobDocument => {
                        jobDocument.containsKey(GitlabConstants.AttributeId) match {
                            case true => jobDocument.get(GitlabConstants.AttributeId) match {
                                case Some(idAttribute: BsonValue) => idAttribute.isInt32() match {
                                    case true => Some(idAttribute.asInt32())
                                    case false => None
                                }
                                case None => None
                            }
                            case false => None
                        }
                    }).flatten
                )
            }
            case None => None
        }
    }

    private def addJobData(pipelineDocument: BsonDocument, pipelineId: Int): BsonDocument = {
        options.includeJobs match {
            case true => {
                val pipelineJobIds: Option[Array[BsonInt32]] = getJobIds(fetchJobData(pipelineId))
                pipelineJobIds match {
                    case Some(jobIds: Array[BsonInt32]) => pipelineDocument.append(
                        GitlabConstants.AttributeLinks,
                        BsonDocument(GitlabConstants.AttributeJobs -> BsonArray(jobIds))
                    )
                    case None => pipelineDocument
                }
            }
            case false => pipelineDocument
        }
    }

    private def fetchJobData(pipelineId: Int): Option[Array[Document]] = {
        val jobsOptions: GitlabPipelineOptions = GitlabPipelineOptions(
            hostServer = options.hostServer,
            mongoDatabase = options.mongoDatabase,
            projectName = options.projectName,
            pipelineId = pipelineId,
            includeJobLogs = options.includeJobLogs
        )

        (new GitlabPipelineJobsHandler(jobsOptions)).process()
    }
}
