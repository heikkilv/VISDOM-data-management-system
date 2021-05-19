package visdom.fetchers.gitlab

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
        val baseCommitResults: Either[String, Vector[JsonObject]] = super.processResponse(response)
        utils.JsonUtils.modifyJsonResult(
            baseCommitResults,
            utils.JsonUtils.addProjectName,
            options.projectName
        )
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
}
