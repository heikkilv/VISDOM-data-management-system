package visdom.adapters.general.model.events

import java.time.ZonedDateTime
import java.time.ZoneId
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.data.PipelineJobData
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.schemas.PipelineJobSchema
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.utils.TimeUtils


class PipelineJobEvent(
    pipelineJobSchema: PipelineJobSchema,
    projectName: String
)
extends Event {
    def getType: String = PipelineJobEvent.PipelineJobEventType
    val duration: Double = pipelineJobSchema.duration.getOrElse(0.0)

    val origin: ItemLink =
        new GitlabOrigin(
            pipelineJobSchema.host_name,
            CommonConstants.EmptyString,
            projectName,
            None
        ).link

    val author: ItemLink =
        new GitlabAuthor(
            authorName = pipelineJobSchema.user.name,
            authorEmail = CommonConstants.EmptyString,
            hostName = pipelineJobSchema.host_name,
            authorDescription = None,
            userId = Some(pipelineJobSchema.user.id),
            relatedCommitEventIds = Seq.empty
        ).link

    val data: PipelineJobData = PipelineJobData.fromPipelineJobSchema(pipelineJobSchema)

    val message: String = pipelineJobSchema.status
    val time: ZonedDateTime = PipelineEvent.toZonedDateTime(
        pipelineJobSchema.started_at match {
            case Some(startedAt: String) => startedAt
            case None => pipelineJobSchema.created_at
        }
    )

    val id: String = PipelineJobEvent.getId(origin.id, data.job_id)

    // add a link to the author
    addRelatedConstructs(Seq(author))

    // add links to the related pipeline and commit events
    addRelatedEvents(
        Seq(
            ItemLink(PipelineEvent.getId(origin.id, data.pipeline_id), PipelineEvent.PipelineEventType),
            ItemLink(CommitEvent.getId(origin.id, data.commit_id), CommitEvent.CommitEventType)
        )
    )
}

object PipelineJobEvent {
    final val PipelineJobEventType: String = "pipeline_job"

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

    def fromPipelineJobSchema(pipelineJobSchema: PipelineJobSchema, projectName: String): PipelineJobEvent = {
        new PipelineJobEvent(pipelineJobSchema, projectName)
    }

    def getId(originId: String, jobId: Int): String = {
        GeneralUtils.getUuid(originId, PipelineJobEventType, jobId.toString())
    }

    def getId(hostName: String, projectName: String, jobId: Int): String = {
        getId(
            originId = GitlabOrigin.getId(hostName, projectName),
            jobId = jobId
        )
    }
}
