package visdom.adapters.general.model.authors

import visdom.adapters.general.model.authors.data.CommitAuthorData
import visdom.adapters.general.model.authors.states.AuthorState
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.utils.GeneralUtils


class CommitAuthor(
    authorName: String,
    authorEmail: String,
    hostName: String,
    relatedCommitEventIds: Seq[String],
    relatedAuthorIds: Seq[String]
)
extends Author {
    override def getType: String = CommitAuthor.CommitAuthorType

    val name: String = authorName
    val description: String = Author.DefaultDescription
    val state: String = AuthorState.ActiveAuthorStateString  // NOTE: use active author state for everyone
    val origin: ItemLink = GitlabOrigin.getGitlabOriginFromHost(hostName).link
    val data: CommitAuthorData = CommitAuthorData(authorEmail)

    val id: String = CommitAuthor.getId(origin.id, authorEmail)

    addRelatedConstructs(
        relatedAuthorIds.map(
            authorId => ItemLink(authorId, GitlabAuthor.GitlabAuthorType)
        )
    )

    addRelatedEvents(
        relatedCommitEventIds.map(
            commitEventId => ItemLink(commitEventId, CommitEvent.CommitEventType)
        )
    )
}

object CommitAuthor {
    final val CommitAuthorType: String = "committer"

    def getId(originId: String, authorEmail: String): String = {
        GeneralUtils.getUuid(originId, CommitAuthorType, authorEmail)
    }
}
