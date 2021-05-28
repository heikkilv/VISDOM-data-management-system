package visdom.fetchers.gitlab

import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest


class GitlabCommitRefsHandler(options: GitlabCommitLinkOptions)
    extends GitlabCommitLinkHandler {
    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/commits.html#get-references-a-commit-is-pushed-to
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathRepository,
            GitlabConstants.PathCommits,
            options.commitId,
            GitlabConstants.PathRefs
        ).mkString("/")

        options.hostServer.modifyRequest(Http(uri))
    }
}
