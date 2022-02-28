package visdom.adapters.general.model.events

import java.time.ZonedDateTime
import java.time.ZoneId
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.events.data.PipelineData
import visdom.adapters.general.schemas.PipelineSchema
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.utils.TimeUtils


class PipelineEvent(
    pipelineSchema: PipelineSchema
)
extends Event {
    def getType: String = PipelineEvent.PipelineEventType
    val duration: Double = pipelineSchema.duration

    val origin: ItemLink =
        new GitlabOrigin(
            pipelineSchema.host_name,
            pipelineSchema.group_name,
            pipelineSchema.project_name,
            None
        ).link

    val author: ItemLink =
        new GitlabAuthor(
            authorName = pipelineSchema.user.name,
            authorEmail = pipelineSchema.user.username,  // TODO: fix this
            hostName = pipelineSchema.host_name,
            authorDescription = None,
            userId = Some(pipelineSchema.user.id),
            relatedCommitEventIds = Seq.empty
        ).link

    val data: PipelineData = PipelineData.fromPipelineSchema(pipelineSchema)

    val message: String = pipelineSchema.status
    val time: ZonedDateTime = PipelineEvent.toZonedDateTime(pipelineSchema.started_at)

    val id: String = PipelineEvent.getId(origin.id, data.pipeline_id)

    // TODO: add construct and event links
}

object PipelineEvent {
    final val PipelineEventType: String = "pipeline"

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

    def fromPipelineSchema(pipelineSchema: PipelineSchema): PipelineEvent = {
        new PipelineEvent(pipelineSchema)
    }

    def getId(originId: String, pipelineId: Int): String = {
        GeneralUtils.getUuid(originId, PipelineEventType, pipelineId.toString())
    }

    def getId(hostName: String, projectName: String, pipelineId: Int): String = {
        getId(
            originId = GitlabOrigin.getId(hostName, projectName),
            pipelineId = pipelineId
        )
    }
}
