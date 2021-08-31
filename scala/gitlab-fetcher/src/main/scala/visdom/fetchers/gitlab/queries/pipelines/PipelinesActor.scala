package visdom.fetchers.gitlab.queries.pipelines

import akka.actor.Actor
import akka.actor.ActorLogging
import java.time.ZonedDateTime
import org.mongodb.scala.bson.collection.immutable.Document
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import visdom.fetchers.gitlab.GitlabConstants
import visdom.fetchers.gitlab.GitlabPipelinesHandler
import visdom.fetchers.gitlab.GitlabPipelinesOptions
import visdom.fetchers.gitlab.PipelinesSpecificFetchParameters
import visdom.fetchers.gitlab.Routes.server
import visdom.fetchers.gitlab.Routes.targetDatabase
import visdom.fetchers.gitlab.queries.CommonHelpers
import visdom.fetchers.gitlab.queries.Constants
import visdom.http.server.ResponseUtils
import visdom.http.server.ServerProtocol
import visdom.http.server.fetcher.gitlab.PipelinesQueryOptions
import visdom.http.server.response.StatusResponse
import visdom.utils.WartRemoverConstants


class PipelinesActor extends Actor with ActorLogging with ServerProtocol {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: PipelinesQueryOptions => {
            log.info(s"Received pipelines query with options: ${queryOptions.toString()}")
            val response: StatusResponse = PipelinesActor.getFetchOptions(queryOptions) match {
                case Right(fetchParameters: PipelinesSpecificFetchParameters) => {
                    CommonHelpers.checkProjectAvailability(fetchParameters.projectName) match {
                        case GitlabConstants.StatusCodeOk => {
                            // start the pipeline data fetching
                            val commitFetching = Future(PipelinesActor.startPipelineFetching(fetchParameters))

                            ResponseUtils.getAcceptedResponse(
                                PipelinesConstants.PipelinesStatusAcceptedDescription,
                                queryOptions.toJsObject()
                            )
                        }
                        case GitlabConstants.StatusCodeUnauthorized => ResponseUtils.getUnauthorizedResponse(
                            s"Access to project '${fetchParameters.projectName}' not allowed"
                        )
                        case GitlabConstants.StatusCodeNotFound => ResponseUtils.getNotFoundResponse(
                            s"Project '${fetchParameters.projectName}' not found"
                        )
                        case _ => ResponseUtils.getErrorResponse(Constants.StatusErrorDescription)
                    }
                }
                case Left(errorDescription: String) => ResponseUtils.getErrorResponse(errorDescription)
            }
            sender() ! response
        }
    }
}

object PipelinesActor {
    // scalastyle:off cyclomatic.complexity
    def getFetchOptions(queryOptions: PipelinesQueryOptions): Either[String, PipelinesSpecificFetchParameters] = {
        val startDate: Option[ZonedDateTime] = CommonHelpers.toZonedDateTime(queryOptions.startDate)
        val endDate: Option[ZonedDateTime] = CommonHelpers.toZonedDateTime(queryOptions.endDate)

        if (!CommonHelpers.isProjectName(queryOptions.projectName)) {
            Left(s"'${queryOptions.projectName}' is not a valid project name")
        }
        else if (!CommonHelpers.isReference(queryOptions.reference)) {
            Left(s"'${queryOptions.reference}' is not a valid reference for a project")
        }
        else if (queryOptions.startDate.isDefined && !startDate.isDefined) {
            Left(s"'${queryOptions.startDate.getOrElse("")}' is not valid datetime in ISO 8601 format with timezone")
        }
        else if (queryOptions.endDate.isDefined && !endDate.isDefined) {
            Left(s"'${queryOptions.endDate.getOrElse("")}' is not valid datetime in ISO 8601 format with timezone")
        }
        else if (startDate.isDefined && endDate.isDefined && !CommonHelpers.lessOrEqual(startDate, endDate)) {
            Left("the endDate must be later than the startDate")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.includeReports)) {
            Left(s"'${queryOptions.includeReports}' is not valid value for includeReports")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.includeJobs)) {
            Left(s"'${queryOptions.includeJobs}' is not valid value for includeJobs")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.includeJobLogs)) {
            Left(s"'${queryOptions.includeJobLogs}' is not valid value for includeJobLogs")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.useAnonymization)) {
            Left(s"'${queryOptions.useAnonymization}' is not valid value for useAnonymization")
        }
        else {
            Right(PipelinesSpecificFetchParameters(
                projectName = queryOptions.projectName,
                reference = queryOptions.reference,
                startDate = startDate,
                endDate = endDate,
                includeReports = queryOptions.includeReports.toBoolean,
                includeJobs = queryOptions.includeJobs.toBoolean,
                includeJobLogs = queryOptions.includeJobLogs.toBoolean,
                useAnonymization = queryOptions.useAnonymization.toBoolean
            ))
        }
    }
    // scalastyle:on cyclomatic.complexity

    def startPipelineFetching(fetchParameters: PipelinesSpecificFetchParameters): Unit = {
        val pipelineFetcherOptions: GitlabPipelinesOptions = GitlabPipelinesOptions(
            hostServer = server,
            mongoDatabase = Some(targetDatabase),
            projectName = fetchParameters.projectName,
            reference = fetchParameters.reference,
            startDate = fetchParameters.startDate,
            endDate = fetchParameters.endDate,
            includeReports = fetchParameters.includeReports,
            includeJobs = fetchParameters.includeJobs,
            includeJobLogs = fetchParameters.includeJobLogs,
            useAnonymization = fetchParameters.useAnonymization
        )
        val pipelineFetcher = new GitlabPipelinesHandler(pipelineFetcherOptions)
        val pipelineCount = pipelineFetcher.process() match {
            case Some(documents: Array[Document]) => documents.size
            case None => 0
        }
        println(s"Found ${pipelineCount} pipelines from project '${fetchParameters.projectName}'")
    }
}
