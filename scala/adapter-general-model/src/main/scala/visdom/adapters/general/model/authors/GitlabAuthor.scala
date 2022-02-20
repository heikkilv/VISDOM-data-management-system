package visdom.adapters.general.model.authors

import visdom.adapters.general.model.authors.data.GitlabAuthorData
import visdom.adapters.general.model.authors.states.AuthorState
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.utils.GeneralUtils


class GitlabAuthor(
    authorName: String,
    authorEmail: String,
    hostName: String,
    authorDescription: Option[String],
    userId: Option[Int],
    relatedCommitEventIds: Seq[String]
)
extends Author {
    override def getType: String = GitlabAuthor.GitlabAuthorType

    val name: String = authorName
    val description: String = authorDescription match {
        case Some(authorText: String) => authorText
        case None => Author.DefaultDescription
    }
    val state: String = AuthorState.ActiveAuthorStateString  // NOTE: use active author state for everyone
    val origin: ItemLink = GitlabOrigin.getGitlabOriginFromHost(hostName).link
    val data: GitlabAuthorData = GitlabAuthorData(
        id = userId,
        email = authorEmail
    )

    val id: String = GeneralUtils.getUuid(
        origin.id,
        userId match {
            case Some(userIdInt: Int) => userIdInt.toString()
            case None => authorEmail
        }
    )

    addRelatedEvents(
        relatedCommitEventIds.map(
            commitEventId => ItemLink(
                id = commitEventId,
                `type` = CommitEvent.CommitEventType
            )
        )
    )
}

object GitlabAuthor {
    final val GitlabAuthorType: String = "author"
}
