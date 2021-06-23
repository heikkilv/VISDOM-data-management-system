package visdom.fetchers.gitlab.queries.pipelines

import akka.actor.Actor
import akka.actor.ActorLogging
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
import visdom.http.server.response.StatusResponse
import visdom.http.server.ResponseUtils
import visdom.http.server.ServerProtocol
import visdom.http.server.fetcher.gitlab.PipelinesQueryOptions


class PipelinesActor extends Actor with ActorLogging with ServerProtocol {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
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
    def getFetchOptions(queryOptions: PipelinesQueryOptions): Either[String, PipelinesSpecificFetchParameters] = {
        if (!CommonHelpers.isProjectName(queryOptions.projectName)) {
            Left(s"'${queryOptions.projectName}' is not a valid project name")
        }
        else {
            Right(PipelinesSpecificFetchParameters(
                projectName = queryOptions.projectName
            ))
        }
    }

    def startPipelineFetching(fetchParameters: PipelinesSpecificFetchParameters): Unit = {
        val pipelineFetcherOptions: GitlabPipelinesOptions = GitlabPipelinesOptions(
            hostServer = server,
            mongoDatabase = Some(targetDatabase),
            projectName = fetchParameters.projectName
        )
        val pipelineFetcher = new GitlabPipelinesHandler(pipelineFetcherOptions)
        val pipelineCount = pipelineFetcher.process() match {
            case Some(documents: Array[Document]) => documents.size
            case None => 0
        }
        println(s"Found ${pipelineCount} pipelines from project '${fetchParameters.projectName}'")
    }
}
