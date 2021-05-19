package visdom.fetchers.gitlab

import io.circe.Json
import io.circe.JsonObject
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse


class GitlabFileHandler(options: GitlabFileOptions)
    extends GitlabDataHandler {

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/repositories.html#list-repository-tree
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathRepository,
            GitlabConstants.PathTree
        ).mkString("/")

        val commitRequest: HttpRequest = processOptionalParameters(
            Http(uri).param(GitlabConstants.ParamRef, options.reference)
        )
        options.hostServer.modifyRequest(commitRequest)
    }

    override def processResponse(response: HttpResponse[String]): Either[String, Vector[JsonObject]] = {
        val baseFileResults: Either[String, Vector[JsonObject]] = super.processResponse(response)
        val modifiedFileResults: Either[String, Vector[JsonObject]] = utils.JsonUtils.modifyJsonResult(
            baseFileResults,
            utils.JsonUtils.addProjectName,
            options.projectName
        )

        modifiedFileResults match {
            case Right(fileResults: Vector[JsonObject]) => {
                options.includeCommitLinks match {
                    case Some(includeCommitLinks: Boolean) if includeCommitLinks => {
                        Right(fetchAllLinkData(fileResults))
                    }
                    case _ => modifiedFileResults
                }
            }
            case Left(_) => modifiedFileResults
        }
    }

    private def processOptionalParameters(request: HttpRequest): HttpRequest = {
        @SuppressWarnings(Array("org.wartremover.warts.Var"))
        var paramMap: Seq[(String, String)] = Seq.empty

        options.filePath match {
            case Some(filePath: String) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamPath, urlEncode(filePath, utf8)
                ))
            }
            case None =>
        }

        options.useRecursiveSearch match {
            case Some(useRecursiveSearch: Boolean) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamRecursive,
                    useRecursiveSearch.toString()
                ))
            }
            case None =>
        }

        // includeCommitLinks

        request.params(paramMap)
    }

    private def fetchLinkData(filePath: String): Either[String, Vector[JsonObject]] = {
        val commitOptions: GitlabCommitOptions = GitlabCommitOptions(
            options.hostServer,
            options.projectName,
            options.reference,
            None,
            None,
            Some(filePath),
            None,
            None,
            None
        )
        val commitFetcher: GitlabCommitHandler = new GitlabCommitHandler(commitOptions)
        val commitRequest: HttpRequest = commitFetcher.getRequest()
        val commitResponses: Vector[HttpResponse[String]] =
            commitFetcher.makeRequests(commitRequest)

        commitFetcher.processAllResponses(commitResponses)
    }

    private def fetchAllLinkData(fileData: Vector[JsonObject]): Vector[JsonObject] = {
        fileData.map(
            fileObject => {
                fileObject.apply(GitlabConstants.AttributePath) match {
                    case Some(pathJson: Json) => pathJson.asString match {
                        case Some(filePath: String) => fetchLinkData(filePath) match {
                            case Right(commitData: Vector[JsonObject]) => {
                                // only pick the commit ids as a list
                                val commitJson: Json = Json.fromValues(
                                    commitData.map(
                                        commitObject => commitObject.apply(GitlabConstants.AttributeId) match {
                                            case Some(commitIdJson: Json) => commitIdJson.asString
                                            case None => None
                                        }
                                    ).flatten.map(commitId => Json.fromString(commitId))
                                )
                                utils.JsonUtils.addSubAttribute(
                                    fileObject,
                                    GitlabConstants.AttributeLinks,
                                    GitlabConstants.AttributeCommits,
                                    commitJson
                                )
                            }
                            case Left(errorMessage: String) => fileObject
                        }
                        case None => fileObject
                    }
                    case None => fileObject
                }
            }
        )
    }
}
