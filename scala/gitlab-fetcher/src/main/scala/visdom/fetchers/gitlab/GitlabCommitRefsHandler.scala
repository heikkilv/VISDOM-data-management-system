// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
    override val createMetadataDocument: Boolean = false

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
