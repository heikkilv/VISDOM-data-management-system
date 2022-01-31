package visdom.adapters.general.model.events

import java.time.ZonedDateTime
import java.time.ZoneId
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.events.data.CommitData
import visdom.adapters.general.schemas.CommitSchema
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.adapters.general.model.artifacts.FileArtifact


class CommitEvent(
    commitSchema: CommitSchema
)
extends Event {
    def getType: String = CommitEvent.CommitEventType
    val duration: Double = 0.0

    val origin: ItemLink =
        new GitlabOrigin(
            commitSchema.host_name,
            commitSchema.group_name,
            commitSchema.project_name
        ).link

    val author: ItemLink =
        new GitlabAuthor(
            authorName = commitSchema.committer_name,
            authorEmail = commitSchema.committer_email,
            hostName = commitSchema.host_name,
            authorDescription = None,
            userId = None
        ).link

    val data: CommitData = CommitData.fromCommitSchema(commitSchema)

    val message: String = commitSchema.message
    val time: ZonedDateTime = CommitEvent.toZonedDateTime(commitSchema.committed_date)

    val id: String = CommitEvent.getId(origin.id, data.commitId)

    // author and linked files as related constructs
    addRelatedConstructs(Seq(author))
    addRelatedConstructs(
        data.files.map(
            filePath => ItemLink(
                id = FileArtifact.getId(origin.id, filePath),
                linkType = FileArtifact.FileArtifactType
            )
        )
    )

    // add parent commits as related events
    addRelatedEvents(
        data.parentIds.map(
            commitId => ItemLink(
                id = CommitEvent.getId(origin.id, commitId),
                linkType = getType
            )
        )
    )
}

object CommitEvent {
    final val CommitEventType: String = "commit"

    final val DefaultTime: ZonedDateTime = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of(CommonConstants.UTC))

    def toZonedDateTime(dateTimeString: String): ZonedDateTime = {
        GeneralUtils.toZonedDateTime(dateTimeString) match {
            case Some(dateTimeValue: ZonedDateTime) => dateTimeValue
            case None => DefaultTime
        }
    }

    def fromCommitSchema(commitSchema: CommitSchema): CommitEvent = {
        new CommitEvent(commitSchema)
    }

    def getId(originId: String, commitId: String): String = {
        GeneralUtils.getUuid(originId, CommitEventType, commitId)
    }
}
