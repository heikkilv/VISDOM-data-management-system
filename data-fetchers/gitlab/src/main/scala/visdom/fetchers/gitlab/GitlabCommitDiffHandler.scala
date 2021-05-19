package visdom.fetchers.gitlab

import io.circe.JsonObject
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse


class GitlabCommitDiffHandler(options: GitlabCommitLinkOptions)
    extends GitlabCommitLinkHandler {
    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/commits.html#get-the-diff-of-a-commit
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathRepository,
            GitlabConstants.PathCommits,
            options.commitId,
            GitlabConstants.PathDiff
        ).mkString("/")

        options.hostServer.modifyRequest(Http(uri))
    }

    override def processResponse(response: HttpResponse[String]): Either[String, Vector[JsonObject]] = {
        val baseCommitResults: Either[String, Vector[JsonObject]] = super.processResponse(response)
        utils.JsonUtils.modifyJsonResult(
            baseCommitResults,
            utils.JsonUtils.removeAttribute,
            GitlabConstants.AttributeDiff
        )
    }
}
