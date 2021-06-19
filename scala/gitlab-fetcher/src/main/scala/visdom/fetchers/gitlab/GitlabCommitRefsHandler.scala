package visdom.fetchers.gitlab

import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import visdom.database.mongodb.MongoConstants


class GitlabCommitRefsHandler(options: GitlabCommitLinkOptions)
    extends GitlabCommitLinkHandler(options) {

    def getFetcherType(): String = GitlabConstants.FetcherTypeCommitRefs
    def getCollectionName(): String = MongoConstants.CollectionTemp

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
