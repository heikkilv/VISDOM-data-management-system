// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.events

import java.time.ZonedDateTime
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.model.artifacts.PipelineReportArtifact
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
    val duration: Double = pipelineSchema.duration.getOrElse(0.0)

    val origin: ItemLink =
        new GitlabOrigin(
            pipelineSchema.host_name,
            pipelineSchema.group_name,
            pipelineSchema.project_name,
            None
        ).link

    val author: ItemLink =
        new GitlabAuthor(
            userId = pipelineSchema.user.id,
            username = pipelineSchema.user.username,
            authorName = pipelineSchema.user.name,
            authorState = pipelineSchema.user.state,
            hostName = pipelineSchema.host_name,
            relatedCommitterIds = Seq.empty,
            relatedCommitEventIds = Seq.empty,
            relatedPipelineEventIds = Seq.empty,
            relatedPipelineJobEventIds = Seq.empty
        ).link

    val data: PipelineData = PipelineData.fromPipelineSchema(pipelineSchema)

    val message: String = pipelineSchema.status
    val time: ZonedDateTime = TimeUtils.toZonedDateTimeWithDefault(
        pipelineSchema.started_at match {
            case Some(startedAt: String) => startedAt
            case None => pipelineSchema.created_at
        }
    )

    val id: String = PipelineEvent.getId(origin.id, data.pipeline_id)

    // add links to the jobs contained in the pipeline
    addRelatedEvents(
        data.jobs.map(
            jobId => ItemLink(
                PipelineJobEvent.getId(origin.id, jobId),
                PipelineJobEvent.PipelineJobEventType
            )
        )
    )

    // add links to the author and the pipeline report
    addRelatedConstructs(
        Seq(
            author,
            ItemLink(
                PipelineReportArtifact.getId(origin.id, data.pipeline_id),
                PipelineReportArtifact.PipelineReportArtifactType
            )
        )
    )
}

object PipelineEvent {
    final val PipelineEventType: String = "pipeline"

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
