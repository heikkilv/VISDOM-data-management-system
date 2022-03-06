package visdom.adapters.general.model.events

import java.time.ZonedDateTime
import java.time.ZoneId
import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.authors.CommitAuthor
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.data.CommitData
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.schemas.CommitSchema
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.utils.TimeUtils


class CommitEvent(
    commitSchema: CommitSchema,
    pipelineJobIds: Seq[Int],
    gitlabAuthorIds: Seq[String]
)
extends Event {
    def getType: String = CommitEvent.CommitEventType
    val duration: Double = 0.0

    val origin: ItemLink =
        new GitlabOrigin(
            commitSchema.host_name,
            commitSchema.group_name,
            commitSchema.project_name,
            None
        ).link

    val author: ItemLink =
        new CommitAuthor(
            authorName = commitSchema.committer_name,
            authorEmail = commitSchema.committer_email,
            hostName = commitSchema.host_name,
            relatedCommitEventIds = Seq.empty
        ).link

    val data: CommitData = CommitData.fromCommitSchema(commitSchema)

    val message: String = commitSchema.message
    val time: ZonedDateTime = CommitEvent.toZonedDateTime(commitSchema.committed_date)

    val id: String = CommitEvent.getId(origin.id, data.commit_id)

    // author and linked files as related constructs
    addRelatedConstructs(
        Seq(author) ++
        gitlabAuthorIds.map(authorId => ItemLink(authorId, GitlabAuthor.GitlabAuthorType)) ++
        data.files.map(
            filePath => ItemLink(
                id = FileArtifact.getId(origin.id, filePath),
                `type` = FileArtifact.FileArtifactType
            )
        )
    )

    // add parent commits as related events
    addRelatedEvents(
        data.parent_ids.map(
            commitId => ItemLink(
                id = CommitEvent.getId(origin.id, commitId),
                `type` = getType
            )
        )
    )

    // add links to related pipeline job events
    addRelatedEvents(
        pipelineJobIds.map(
            jobId => ItemLink(
                id = PipelineJobEvent.getId(origin.id, jobId),
                `type`= PipelineJobEvent.PipelineJobEventType
            )
        )
    )
}

object CommitEvent {
    final val CommitEventType: String = "commit"

    final val DefaultYear: Int = 1970
    final val DefaultMonth: Int = 1
    final val DefaultDay: Int = 1
    final val DefaultTime: ZonedDateTime =
        ZonedDateTime.of(DefaultYear, DefaultMonth, DefaultDay, 0, 0, 0, 0, ZoneId.of(CommonConstants.UTC))

    def toZonedDateTime(dateTimeString: String): ZonedDateTime = {
        TimeUtils.toZonedDateTime(dateTimeString) match {
            case Some(dateTimeValue: ZonedDateTime) => dateTimeValue
            case None => DefaultTime
        }
    }

    def fromCommitSchema(
        commitSchema: CommitSchema,
        pipelineJobIds: Seq[Int],
        gitlabAuthorIds: Seq[String]
    ): CommitEvent = {
        new CommitEvent(commitSchema, pipelineJobIds, gitlabAuthorIds)
    }

    def getId(originId: String, commitId: String): String = {
        GeneralUtils.getUuid(originId, CommitEventType, commitId)
    }

    def getId(hostName: String, projectName: String, commitId: String): String = {
        getId(
            originId = GitlabOrigin.getId(hostName, projectName),
            commitId = commitId
        )
    }
}
