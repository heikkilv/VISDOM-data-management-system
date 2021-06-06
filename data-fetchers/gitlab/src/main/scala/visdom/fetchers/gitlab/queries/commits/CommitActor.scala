package visdom.fetchers.gitlab.queries.commits

import akka.actor.Actor
import akka.actor.ActorLogging
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import visdom.fetchers.gitlab.CommitSpecificFetchParameters
import visdom.fetchers.gitlab.GitlabCommitHandler
import visdom.fetchers.gitlab.GitlabCommitOptions
import visdom.fetchers.gitlab.GitlabConstants
import visdom.fetchers.gitlab.Routes.server
import visdom.fetchers.gitlab.Routes.targetDatabase
import visdom.fetchers.gitlab.utils.HttpUtils
import org.mongodb.scala.bson.collection.immutable.Document
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import visdom.fetchers.gitlab.queries.GitlabResponse
import visdom.fetchers.gitlab.queries.GitlabResponseAccepted
import visdom.fetchers.gitlab.queries.GitlabResponseProblem


class CommitActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    def receive: Receive = {
        case queryOptions: CommitQueryOptions => {
            log.info(s"Received commits query with options: ${queryOptions.toString()}")
            val response: GitlabResponse = CommitActor.getFetchOptions(queryOptions) match {
                case Right(fetchParameters: CommitSpecificFetchParameters) => {
                    CommitActor.checkProjectAvailability(fetchParameters.projectName) match {
                        case GitlabConstants.StatusCodeOk => {
                            // start the commit data fetching
                            val commitFetching = Future(CommitActor.startCommitFetching(fetchParameters))

                            GitlabResponseAccepted(
                                CommitConstants.CommitQueryAcceptedStatus,
                                CommitConstants.CommitStatusAcceptedDescription,
                                queryOptions
                            )
                        }
                        case GitlabConstants.StatusCodeUnauthorized => GitlabResponseProblem(
                            CommitConstants.CommitQueryUnauthorizedStatus,
                            s"Access to project '${fetchParameters.projectName}' not allowed"
                        )
                        case GitlabConstants.StatusCodeNotFound => GitlabResponseProblem(
                            CommitConstants.CommitQueryNotFoundStatus,
                            s"Project '${fetchParameters.projectName}' not found"
                        )
                        case _ => GitlabResponseProblem(
                            CommitConstants.CommitQueryErrorStatus,
                            CommitConstants.CommitStatusErrorDescription
                        )
                    }
                }
                case Left(errorDescription: String) => GitlabResponseProblem(
                    CommitConstants.CommitQueryInvalidStatus,
                    errorDescription
                )
            }
            sender() ! response
        }
    }
}

object CommitActor {
    def isProjectName(projectName: String): Boolean = {
        projectName != ""
    }

    def isReference(reference: String): Boolean = {
        reference != ""
    }

    def toZonedDateTime(dateTimeStringOption: Option[String]): Option[ZonedDateTime] = {
        dateTimeStringOption match {
            case Some(dateTimeString: String) =>
                try {
                    Some(ZonedDateTime.parse(dateTimeString))
                }
                catch {
                    case error: DateTimeParseException => None
                }
            case None => None
        }
    }

    def toFilePath(filePathOption: Option[String]): Option[String] = {
        filePathOption match {
            case Some(filePath: String) => filePath match {
                case "" => None
                case _ => filePathOption
            }
            case None => None
        }
    }

    def lessOrEqual(dateTimeA: Option[ZonedDateTime], dateTimeB: Option[ZonedDateTime]): Boolean = {
        dateTimeA match {
            case Some(valueA: ZonedDateTime) => dateTimeB match {
                case Some(valueB: ZonedDateTime) => valueA.compareTo(valueB) <= 0
                case None => false
            }
            case None => false
        }
    }

    // scalastyle:off cyclomatic.complexity
    def getFetchOptions(queryOptions: CommitQueryOptions): Either[String, CommitSpecificFetchParameters] = {
        val startDate: Option[ZonedDateTime] = toZonedDateTime(queryOptions.startDate)
        val endDate: Option[ZonedDateTime] = toZonedDateTime(queryOptions.endDate)
        val filePath: Option[String] = toFilePath(queryOptions.filePath)

        if (!isProjectName(queryOptions.projectName)) {
            Left(s"'${queryOptions.projectName}' is not a valid project name")
        }
        else if (!isReference(queryOptions.reference)) {
            Left(s"'${queryOptions.reference}' is not a valid reference for a project")
        }
        else if (queryOptions.startDate.isDefined && !startDate.isDefined) {
            Left(s"'${queryOptions.startDate.getOrElse("")}' is not valid datetime in ISO 8601 format with timezone")
        }
        else if (queryOptions.endDate.isDefined && !endDate.isDefined) {
            Left(s"'${queryOptions.endDate.getOrElse("")}' is not valid datetime in ISO 8601 format with timezone")
        }
        else if (startDate.isDefined && endDate.isDefined && !lessOrEqual(startDate, endDate)) {
            Left("the endDate must be later than the startDate")
        }
        else if (queryOptions.filePath.isDefined && !filePath.isDefined) {
            Left(s"'${queryOptions.filePath.getOrElse("")}' is not valid path for a file or folder")
        }
        else if (!CommitConstants.BooleanStrings.contains(queryOptions.includeStatistics)) {
            Left(s"'${queryOptions.includeStatistics}' is not valid value for includeStatistics")
        }
        else if (!CommitConstants.BooleanStrings.contains(queryOptions.includeFileLinks)) {
            Left(s"'${queryOptions.includeFileLinks}' is not valid value for includeFileLinks")
        }
        else if (!CommitConstants.BooleanStrings.contains(queryOptions.includeReferenceLinks)) {
            Left(s"'${queryOptions.includeReferenceLinks}' is not valid value for includeReferenceLinks")
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
                includeReferenceLinks = queryOptions.includeReferenceLinks.toBoolean
            ))
        }
    }
    // scalastyle:on cyclomatic.complexity

    def checkProjectAvailability(projectName: String): Int = {
        HttpUtils.getProjectQueryStatusCode(server, projectName)
    }

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
            includeReferenceLinks = fetchParameters.includeReferenceLinks
        )
        val commitFetcher = new GitlabCommitHandler(commitFetcherOptions)
        val commitCount = commitFetcher.process() match {
            case Some(documents: Array[Document]) => documents.size
            case None => 0
        }
        println(s"Found ${commitCount} commits from project '${fetchParameters.projectName}'")
    }
}
