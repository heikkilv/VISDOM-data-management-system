package visdom.fetchers.gitlab

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils


class GitlabCommitDiffHandler(options: GitlabCommitLinkOptions)
    extends GitlabCommitLinkHandler(options) {

    def getFetcherType(): String = GitlabConstants.FetcherTypeCommitDiff
    def getCollectionName(): String = MongoConstants.CollectionTemp
    override val createMetadataDocument: Boolean = false

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

    override def processDocument(document: BsonDocument): BsonDocument = {
        JsonUtils.removeAttribute(document, GitlabConstants.AttributeDiff)
    }
}
