package visdom.fetchers.gitlab

import io.circe.JsonObject
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse


class GitlabFileHandler(
    hostServer: GitlabServer,
    projectName: String,
    reference: String,
    useRecursiveSearch: Boolean
) extends GitlabDataHandler {

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/repositories.html
        val uri: String = List(
            hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(projectName, utf8),
            GitlabConstants.PathRepository,
            GitlabConstants.PathTree
        ).mkString("/")

        val params: Map[String, String] = Map(
            GitlabConstants.ParamRef -> reference,
            GitlabConstants.ParamRecursive -> useRecursiveSearch.toString()
        )

        val commitRequest: HttpRequest = Http(uri).params(params)
        hostServer.modifyRequest(commitRequest)
    }

    override def processResponse(response: HttpResponse[String]): Either[String, Vector[JsonObject]] = {
        val baseCommitResults: Either[String, Vector[JsonObject]] = super.processResponse(response)
        utils.JsonUtils.modifyJsonResult(baseCommitResults, utils.JsonUtils.addProjectName, projectName)
    }
}
