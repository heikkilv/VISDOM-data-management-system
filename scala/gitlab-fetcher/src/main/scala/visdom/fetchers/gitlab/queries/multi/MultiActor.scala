package visdom.fetchers.gitlab.queries.multi

import akka.actor.Actor
import akka.actor.ActorLogging
import java.time.ZonedDateTime
import scala.concurrent.ExecutionContext
import visdom.fetchers.FetcherUtils
import visdom.fetchers.gitlab.MultiSpecificFetchParameters
import visdom.fetchers.gitlab.MultiSpecificSingleFetchParameters
import visdom.fetchers.gitlab.CommitSpecificFetchParameters
import visdom.fetchers.gitlab.FileSpecificFetchParameters
import visdom.fetchers.gitlab.GitlabConstants.StatusCodeOk
import visdom.fetchers.gitlab.GitlabConstants.StatusCodeUnauthorized
import visdom.fetchers.gitlab.queries.CommonHelpers
import visdom.fetchers.gitlab.queries.Constants
import visdom.fetchers.gitlab.queries.commits.CommitActor
import visdom.fetchers.gitlab.queries.files.FileActor
import visdom.http.server.fetcher.gitlab.MultiQueryOptions
import visdom.http.server.fetcher.gitlab.Projects
import visdom.http.server.response.StatusResponse
import visdom.http.server.ResponseUtils
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.utils.WartRemoverConstants


class MultiActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: MultiQueryOptions => {
            log.info(s"Received multi data query with options: ${queryOptions.toString()}")
            val response: StatusResponse = MultiActor.getFetchOptions(queryOptions) match {
                case Right(fetchParameters: MultiSpecificFetchParameters) => {
                    fetchParameters.projectNames.nonEmpty match {
                        case true => {
                            // start the data fetching
                            MultiActor.startDataFetching(fetchParameters)
                            // return the accepted response
                            ResponseUtils.getAcceptedResponse(
                                MultiConstants.MultiStatusAcceptedDescription,
                                queryOptions.toJsObject(fetchParameters.projects)
                            )
                        }
                        case false => fetchParameters.projects.unauthorized.nonEmpty match {
                            case true => ResponseUtils.getUnauthorizedResponse(
                                "No available projects found. Access to projects '" +
                                s"${fetchParameters.projects.unauthorized.mkString(
                                    CommonConstants.Comma + CommonConstants.WhiteSpace
                                )}' not allowed"
                            )
                            case false => ResponseUtils.getNotFoundResponse("No available projects found")
                        }
                    }
                }
                case Left(errorDescription: String) => ResponseUtils.getInvalidResponse(errorDescription)
            }
            sender() ! response
        }
    }
}

object MultiActor {
    // scalastyle:off cyclomatic.complexity
    def checkQueryOptions(queryOptions: MultiQueryOptions): Option[String] = {
        val filePath: Option[String] = CommonHelpers.toFilePath(queryOptions.filePath)
        val startDate: Option[ZonedDateTime] = GeneralUtils.toZonedDateTime(queryOptions.startDate)
        val endDate: Option[ZonedDateTime] = GeneralUtils.toZonedDateTime(queryOptions.endDate)

        if (!CommonHelpers.isProjectNameSequence(queryOptions.projectNames)) {
            Some(s"'${queryOptions.projectNames}' is not a valid comma-separated list of project names")
        }
        else if (queryOptions.filePath.isDefined && !filePath.isDefined) {
            Some(s"'${queryOptions.filePath.getOrElse("")}' is not valid path for a file or folder")
        }
        else if (queryOptions.startDate.isDefined && !startDate.isDefined) {
            Some(s"'${queryOptions.startDate.getOrElse("")}' is not valid datetime in ISO 8601 format with timezone")
        }
        else if (queryOptions.endDate.isDefined && !endDate.isDefined) {
            Some(s"'${queryOptions.endDate.getOrElse("")}' is not valid datetime in ISO 8601 format with timezone")
        }
        else if (startDate.isDefined && endDate.isDefined && !GeneralUtils.lessOrEqual(startDate, endDate)) {
            Some("the endDate must be later than the startDate")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.useAnonymization)) {
            Some(s"'${queryOptions.useAnonymization}' is not valid value for useAnonymization")
        }
        else {
            None
        }
    }
    // scalastyle:on cyclomatic.complexity

    def getFetchOptions(queryOptions: MultiQueryOptions): Either[String, MultiSpecificFetchParameters] = {
        checkQueryOptions(queryOptions) match {
            case Some(errorMessage: String) => Left(errorMessage)
            case None => {
                val projectNamesAll: Seq[String] = queryOptions.projectNames.split(CommonConstants.Comma)
                val projectAvailability: Projects = getAvailabilityForProjects(projectNamesAll)

                Right(
                    MultiSpecificFetchParameters(
                        projectNames = projectAvailability.allowed,
                        filePath = queryOptions.filePath,
                        startDate = GeneralUtils.toZonedDateTime(queryOptions.startDate),
                        endDate = GeneralUtils.toZonedDateTime(queryOptions.endDate),
                        useAnonymization = queryOptions.useAnonymization.toBoolean,
                        projects = projectAvailability
                    )
                )
            }
        }
    }

    def getAvailabilityForProjects(projectNames: Seq[String]): Projects = {
        val codeMap = CommonHelpers.checkProjectsAvailability(projectNames)
        Projects(
            allowed = codeMap.getOrElse(StatusCodeOk, Seq.empty),
            unauthorized = codeMap.getOrElse(StatusCodeUnauthorized, Seq.empty),
            notFound =
                codeMap.filterKeys(key => key != StatusCodeOk && key != StatusCodeUnauthorized)
                    .toSeq
                    .map({ case (code, names) => names })
                    .reduceOption((names1, names2) => names1 ++ names2) match {
                        case Some(notFoundProjects: Seq[String]) => notFoundProjects
                        case None => Seq.empty
                    }
        )
    }

    def startCommitFetching(fetchParameters: MultiSpecificSingleFetchParameters): Unit = {
        val commitFetchParameters = CommitSpecificFetchParameters(
            projectName = fetchParameters.projectName,
            reference = Constants.ParameterDefaultReference,
            filePath = fetchParameters.filePath,
            startDate = fetchParameters.startDate,
            endDate = fetchParameters.endDate,
            includeStatistics = MultiConstants.ParameterDefaultIncludeStatistics,
            includeFileLinks = MultiConstants.ParameterDefaultIncludeFileLinks,
            includeReferenceLinks = MultiConstants.ParameterDefaultIncludeReferenceLinks,
            useAnonymization = fetchParameters.useAnonymization
        )
        CommitActor.startCommitFetching(commitFetchParameters)
    }

    def startFileFetching(fetchParameters: MultiSpecificSingleFetchParameters): Unit = {
        val fileFetchParameters = FileSpecificFetchParameters(
            projectName = fetchParameters.projectName,
            reference = Constants.ParameterDefaultReference,
            filePath = fetchParameters.filePath,
            recursive = MultiConstants.ParameterDefaultRecursive,
            includeCommitLinks = MultiConstants.ParameterDefaultIncludeCommitLinks,
            useAnonymization = fetchParameters.useAnonymization
        )
        FileActor.startFileFetching(fileFetchParameters)
    }

    private val dataFetchers: Seq[MultiSpecificSingleFetchParameters => Unit] = Seq(
        MultiActor.startCommitFetching(_),
        MultiActor.startFileFetching(_)
    )

    def startDataFetching(fetchParameters: MultiSpecificFetchParameters): Unit = {
        val singleProjectParameters = fetchParameters.projectNames.map(
            projectName => MultiSpecificSingleFetchParameters(
                projectName = projectName,
                filePath = fetchParameters.filePath,
                startDate = fetchParameters.startDate,
                endDate = fetchParameters.endDate,
                useAnonymization = fetchParameters.useAnonymization
            )
        )
        val (
            fetchers: Seq[MultiSpecificSingleFetchParameters => Unit],
            parameters: Seq[MultiSpecificSingleFetchParameters]
        ) = dataFetchers
                .map(fetcher => singleProjectParameters.map(parameters => (fetcher, parameters)))
                .flatten
                .unzip

        FetcherUtils.handleDataFetchingSequence(fetchers, parameters)
    }
}
