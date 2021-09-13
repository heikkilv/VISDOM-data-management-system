package visdom.fetchers.gitlab.queries.commits

import akka.actor.Actor
import akka.actor.ActorLogging
import java.time.ZonedDateTime
import org.mongodb.scala.bson.collection.immutable.Document
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import visdom.fetchers.gitlab.CommitSpecificFetchParameters
import visdom.fetchers.gitlab.GitlabCommitHandler
import visdom.fetchers.gitlab.GitlabCommitOptions
import visdom.fetchers.gitlab.GitlabConstants
import visdom.fetchers.gitlab.Routes.fetcherList
import visdom.fetchers.gitlab.Routes.server
import visdom.fetchers.gitlab.Routes.targetDatabase
import visdom.fetchers.gitlab.queries.CommonHelpers
import visdom.fetchers.gitlab.queries.Constants
import visdom.http.server.response.StatusResponse
import visdom.http.server.fetcher.gitlab.CommitQueryOptions
import visdom.http.server.ResponseUtils
import visdom.utils.GeneralUtils
import visdom.utils.WartRemoverConstants


class CommitActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: CommitQueryOptions => {
            log.info(s"Received commits query with options: ${queryOptions.toString()}")
            val response: StatusResponse = CommitActor.getFetchOptions(queryOptions) match {
                case Right(fetchParameters: CommitSpecificFetchParameters) => {
                    CommonHelpers.checkProjectAvailability(fetchParameters.projectName) match {
                        case GitlabConstants.StatusCodeOk => {
                            // start the commit data fetching
                            val commitFetching = Future(CommitActor.startCommitFetching(fetchParameters))

                            ResponseUtils.getAcceptedResponse(
                                CommitConstants.CommitStatusAcceptedDescription,
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
                case Left(errorDescription: String) => ResponseUtils.getInvalidResponse(errorDescription)
            }
            sender() ! response
        }
    }
}

object CommitActor {
    // scalastyle:off cyclomatic.complexity
    def getFetchOptions(queryOptions: CommitQueryOptions): Either[String, CommitSpecificFetchParameters] = {
        val startDate: Option[ZonedDateTime] = GeneralUtils.toZonedDateTime(queryOptions.startDate)
        val endDate: Option[ZonedDateTime] = GeneralUtils.toZonedDateTime(queryOptions.endDate)
        val filePath: Option[String] = CommonHelpers.toFilePath(queryOptions.filePath)

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
        else if (startDate.isDefined && endDate.isDefined && !GeneralUtils.lessOrEqual(startDate, endDate)) {
            Left("the endDate must be later than the startDate")
        }
        else if (queryOptions.filePath.isDefined && !filePath.isDefined) {
            Left(s"'${queryOptions.filePath.getOrElse("")}' is not valid path for a file or folder")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.includeStatistics)) {
            Left(s"'${queryOptions.includeStatistics}' is not valid value for includeStatistics")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.includeFileLinks)) {
            Left(s"'${queryOptions.includeFileLinks}' is not valid value for includeFileLinks")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.includeReferenceLinks)) {
            Left(s"'${queryOptions.includeReferenceLinks}' is not valid value for includeReferenceLinks")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.useAnonymization)) {
            Left(s"'${queryOptions.useAnonymization}' is not valid value for useAnonymization")
        }
        else {
            Right(CommitSpecificFetchParameters(
                projectName = queryOptions.projectName,
                reference = queryOptions.reference,
                startDate = startDate,
                endDate = endDate,
                filePath = filePath,
                includeStatistics = queryOptions.includeStatistics.toBoolean,
                includeFileLinks = queryOptions.includeFileLinks.toBoolean,
                includeReferenceLinks = queryOptions.includeReferenceLinks.toBoolean,
                useAnonymization = queryOptions.useAnonymization.toBoolean
            ))
        }
    }
    // scalastyle:on cyclomatic.complexity

    def startCommitFetching(fetchParameters: CommitSpecificFetchParameters): Unit = {
        val commitFetcherOptions: GitlabCommitOptions = GitlabCommitOptions(
            hostServer = server,
            mongoDatabase = Some(targetDatabase),
            projectName = fetchParameters.projectName,
            reference = fetchParameters.reference,
            startDate = fetchParameters.startDate,
            endDate = fetchParameters.endDate,
            filePath = fetchParameters.filePath,
            includeStatistics = fetchParameters.includeStatistics,
            includeFileLinks = fetchParameters.includeFileLinks,
            includeReferenceLinks = fetchParameters.includeReferenceLinks,
            useAnonymization = fetchParameters.useAnonymization
        )
        fetcherList.addFetcher(new GitlabCommitHandler(commitFetcherOptions))
    }
}
