package visdom.fetchers.gitlab.queries.files

import akka.actor.Actor
import akka.actor.ActorLogging
import org.mongodb.scala.bson.collection.immutable.Document
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import visdom.fetchers.gitlab.FileSpecificFetchParameters
import visdom.fetchers.gitlab.GitlabConstants
import visdom.fetchers.gitlab.GitlabFileHandler
import visdom.fetchers.gitlab.GitlabFileOptions
import visdom.fetchers.gitlab.Routes.server
import visdom.fetchers.gitlab.Routes.targetDatabase
import visdom.fetchers.gitlab.queries.CommonHelpers
import visdom.fetchers.gitlab.queries.Constants
import visdom.http.server.response.StatusResponse
import visdom.http.server.fetcher.gitlab.FileQueryOptions
import visdom.http.server.ResponseUtils
import visdom.http.server.ServerProtocol
import visdom.utils.WartRemoverConstants


class FileActor extends Actor with ActorLogging with ServerProtocol {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: FileQueryOptions => {
            log.info(s"Received files query with options: ${queryOptions.toString()}")
            val response: StatusResponse = FileActor.getFetchOptions(queryOptions) match {
                case Right(fetchParameters: FileSpecificFetchParameters) => {
                    CommonHelpers.checkProjectAvailability(fetchParameters.projectName) match {
                        case GitlabConstants.StatusCodeOk => {
                            // start the file data fetching
                            val commitFetching = Future(FileActor.startFileFetching(fetchParameters))

                            ResponseUtils.getAcceptedResponse(
                                FileConstants.FileStatusAcceptedDescription,
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

object FileActor {
    // scalastyle:off cyclomatic.complexity
    def getFetchOptions(queryOptions: FileQueryOptions): Either[String, FileSpecificFetchParameters] = {
        val filePath: Option[String] = CommonHelpers.toFilePath(queryOptions.filePath)

        if (!CommonHelpers.isProjectName(queryOptions.projectName)) {
            Left(s"'${queryOptions.projectName}' is not a valid project name")
        }
        else if (!CommonHelpers.isReference(queryOptions.reference)) {
            Left(s"'${queryOptions.reference}' is not a valid reference for a project")
        }
        else if (queryOptions.filePath.isDefined && !filePath.isDefined) {
            Left(s"'${queryOptions.filePath.getOrElse("")}' is not valid path for a file or folder")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.recursive)) {
            Left(s"'${queryOptions.recursive}' is not valid value for useRecursiveSearch")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.includeCommitLinks)) {
            Left(s"'${queryOptions.includeCommitLinks}' is not valid value for includeCommitLinks")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.useAnonymization)) {
            Left(s"'${queryOptions.useAnonymization}' is not valid value for useAnonymization")
        }
        else {
            Right(FileSpecificFetchParameters(
                projectName = queryOptions.projectName,
                reference = queryOptions.reference,
                filePath = filePath,
                recursive = queryOptions.recursive.toBoolean,
                includeCommitLinks = queryOptions.includeCommitLinks.toBoolean,
                useAnonymization = queryOptions.useAnonymization.toBoolean
            ))
        }
    }
    // scalastyle:on cyclomatic.complexity

    def startFileFetching(fetchParameters: FileSpecificFetchParameters): Unit = {
        val fileFetcherOptions: GitlabFileOptions = GitlabFileOptions(
            hostServer = server,
            mongoDatabase = Some(targetDatabase),
            projectName = fetchParameters.projectName,
            reference = fetchParameters.reference,
            filePath = fetchParameters.filePath,
            recursive = fetchParameters.recursive,
            includeCommitLinks = fetchParameters.includeCommitLinks,
            useAnonymization = fetchParameters.useAnonymization
        )
        val commitFetcher = new GitlabFileHandler(fileFetcherOptions)
        val commitCount = commitFetcher.process() match {
            case Some(documents: Array[Document]) => documents.size
            case None => 0
        }
        println(s"Found ${commitCount} files from project '${fetchParameters.projectName}'")
    }
}
