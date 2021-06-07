package visdom.fetchers.gitlab.queries.all

import akka.actor.Actor
import akka.actor.ActorLogging
import java.time.ZonedDateTime
import org.mongodb.scala.bson.collection.immutable.Document
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success
import visdom.fetchers.gitlab.AllDataSpecificFetchParameters
import visdom.fetchers.gitlab.CommitSpecificFetchParameters
import visdom.fetchers.gitlab.FileSpecificFetchParameters
import visdom.fetchers.gitlab.GitlabConstants
import visdom.fetchers.gitlab.queries.CommonHelpers
import visdom.fetchers.gitlab.queries.Constants
import visdom.fetchers.gitlab.queries.GitlabResponse
import visdom.fetchers.gitlab.queries.GitlabResponseAccepted
import visdom.fetchers.gitlab.queries.GitlabResponseProblem
import visdom.fetchers.gitlab.queries.commits.CommitActor
import visdom.fetchers.gitlab.queries.files.FileActor


class AllDataActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    def receive: Receive = {
        case queryOptions: AllDataQueryOptions => {
            log.info(s"Received all data query with options: ${queryOptions.toString()}")
            val response: GitlabResponse = AllDataActor.getFetchOptions(queryOptions) match {
                case Right(fetchParameters: AllDataSpecificFetchParameters) => {
                    CommonHelpers.checkProjectAvailability(fetchParameters.projectName) match {
                        case GitlabConstants.StatusCodeOk => {
                            // start the commit data fetching
                            val commitFetching: Future[Unit] = Future(AllDataActor.startCommitFetching(fetchParameters))
                            // start the file data fetching after the commit data fetching is complete
                            commitFetching.onComplete({
                                case Success(_) => AllDataActor.startFileFetching(fetchParameters)
                                case Failure(error) => log.error(error.getMessage())
                            })

                            GitlabResponseAccepted(
                                Constants.QueryAcceptedStatus,
                                AllDataConstants.AllDataStatusAcceptedDescription,
                                queryOptions
                            )
                        }
                        case GitlabConstants.StatusCodeUnauthorized => GitlabResponseProblem(
                            Constants.QueryUnauthorizedStatus,
                            s"Access to project '${fetchParameters.projectName}' not allowed"
                        )
                        case GitlabConstants.StatusCodeNotFound => GitlabResponseProblem(
                            Constants.QueryNotFoundStatus,
                            s"Project '${fetchParameters.projectName}' not found"
                        )
                        case _ => GitlabResponseProblem(
                            Constants.QueryErrorStatus,
                            Constants.StatusErrorDescription
                        )
                    }
                }
                case Left(errorDescription: String) => GitlabResponseProblem(
                    Constants.QueryInvalidStatus,
                    errorDescription
                )
            }
            sender() ! response
        }
    }
}

object AllDataActor {
    def getFetchOptions(queryOptions: AllDataQueryOptions): Either[String, AllDataSpecificFetchParameters] = {
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
        else {
            Right(AllDataSpecificFetchParameters(
                projectName = queryOptions.projectName,
                reference = queryOptions.reference,
                startDate = startDate,
                endDate = endDate
            ))
        }
    }

    def startCommitFetching(fetchParameters: AllDataSpecificFetchParameters): Unit = {
        val commitFetchParameters = CommitSpecificFetchParameters(
            projectName = fetchParameters.projectName,
            reference = fetchParameters.reference,
            startDate = fetchParameters.startDate,
            endDate = fetchParameters.endDate,
            filePath = AllDataConstants.ParameterDefaultFilePath,
            includeStatistics = AllDataConstants.ParameterDefaultIncludeStatistics,
            includeFileLinks = AllDataConstants.ParameterDefaultIncludeFileLinks,
            includeReferenceLinks = AllDataConstants.ParameterDefaultIncludeReferenceLinks
        )
        CommitActor.startCommitFetching(commitFetchParameters)
    }

    def startFileFetching(fetchParameters: AllDataSpecificFetchParameters): Unit = {
        val fileFetchParameters = FileSpecificFetchParameters(
            projectName = fetchParameters.projectName,
            reference = fetchParameters.reference,
            filePath = AllDataConstants.ParameterDefaultFilePath,
            recursive = AllDataConstants.ParameterDefaultRecursive,
            includeCommitLinks = AllDataConstants.ParameterDefaultIncludeCommitLinks
        )
        FileActor.startFileFetching(fileFetchParameters)
    }
}
