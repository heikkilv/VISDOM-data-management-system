package visdom.fetchers.gitlab

import io.circe.Json
import io.circe.JsonObject
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse


class GitlabCommitHandler(
    hostServer: GitlabServer,
    projectName: String,
    reference: String,
    includeStatistics: Boolean
) extends GitlabDataHandler {

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/commits.html
        val uri: String = List(
            hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(projectName, utf8),
            GitlabConstants.PathRepository,
            GitlabConstants.PathCommits
        ).mkString("/")

        val params: Map[String, String] = Map(
            GitlabConstants.ParamRef -> reference,
            GitlabConstants.ParamWithStats -> includeStatistics.toString()
        )

        val commitRequest: HttpRequest = Http(uri).params(params)
        hostServer.modifyRequest(commitRequest)
    }

    override def processResponse(response: HttpResponse[String]): Either[String, Vector[JsonObject]] = {
        val baseCommitResults: Either[String, Vector[JsonObject]] = super.processResponse(response)
        utils.JsonUtils.modifyJsonResult(baseCommitResults, utils.JsonUtils.addProjectName, projectName)
    }
}
