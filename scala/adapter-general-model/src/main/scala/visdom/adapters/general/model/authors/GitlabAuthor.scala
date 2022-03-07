package visdom.adapters.general.model.authors

import visdom.adapters.general.model.authors.data.GitlabAuthorData
import visdom.adapters.general.model.authors.states.AuthorState
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.utils.GeneralUtils


class GitlabAuthor(
    userId: Int,
    username: String,
    authorName: String,
    authorState: String,
    hostName: String,
    relatedCommitterIds: Seq[String],
    relatedCommitEventIds: Seq[String],
    relatedPipelineEventIds: Seq[String],
    relatedPipelineJobEventIds: Seq[String]
)
extends Author {
    override def getType: String = GitlabAuthor.GitlabAuthorType

    val name: String = authorName
    val description: String = Author.DefaultDescription
    val state: String = GitlabAuthor.getState(authorState)
    val origin: ItemLink = GitlabOrigin.getGitlabOriginFromHost(hostName).link
    val data: GitlabAuthorData = GitlabAuthorData(
        user_id = userId,
        username = username
    )

    val id: String = GitlabAuthor.getId(origin.id, userId)

    addRelatedConstructs(
        relatedCommitterIds.map(
            committerId => ItemLink(committerId, CommitAuthor.CommitAuthorType)
        )
    )

    addRelatedEvents(
        relatedCommitEventIds.map(
            commitEventId => ItemLink(commitEventId, CommitEvent.CommitEventType)
        ) ++
        relatedPipelineEventIds.map(
            pipelineEventId => ItemLink(pipelineEventId, PipelineEvent.PipelineEventType)
        ) ++
        relatedPipelineJobEventIds.map(
            pipelineJobEventId => ItemLink(pipelineJobEventId, PipelineJobEvent.PipelineJobEventType)
        )
    )
}

object GitlabAuthor {
    final val GitlabAuthorType: String = "gitlab_user"

    def getId(originId: String, userId: Int): String = {
        GeneralUtils.getUuid(originId, GitlabAuthorType, userId.toString())
    }

    def getState(inputState: String): String = {
        inputState match {
            case AuthorState.ActiveAuthorStateString => inputState
            case AuthorState.BlockedAuthorStateString => inputState
            // mark all other input states as "inactive"
            case _ => AuthorState.InActiveAuthorStateString
        }
    }
}
