package visdom.adapters.general.model.events

import java.time.ZonedDateTime
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

    val origin: ItemLink = ItemLink(
        GitlabOrigin.getId(pipelineJobSchema.host_name, projectName),
        GitlabOrigin.GitlabOriginType
    )

    val author: ItemLink = ItemLink(
        GitlabAuthor.getId(GitlabOrigin.getId(pipelineJobSchema.host_name), pipelineJobSchema.user.id),
        GitlabAuthor.GitlabAuthorType
    )

    val data: PipelineJobData = PipelineJobData.fromPipelineJobSchema(pipelineJobSchema)

    val message: String = pipelineJobSchema.status
    val time: ZonedDateTime = TimeUtils.toZonedDateTimeWithDefault(
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
