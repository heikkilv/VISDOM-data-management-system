// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.results

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.base.LinkTrait
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.events.SubmissionEvent
import visdom.adapters.general.model.events.data.CommitData
import visdom.adapters.general.model.events.data.PipelineData
import visdom.adapters.general.model.events.data.PipelineJobData
import visdom.adapters.general.model.events.data.SubmissionData
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.general.schemas.PipelineSchema
import visdom.adapters.general.schemas.PipelineJobSchema
import visdom.adapters.general.schemas.SubmissionSchema
import visdom.adapters.results.BaseResultValue
import visdom.adapters.results.IdValue
import visdom.json.JsonUtils
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class EventResult[EventData <: Data](
    _id: String,
    id: String,
    `type`: String,
    time: String,
    duration: Double,
    message: String,
    origin: ItemLink,
    author: ItemLink,
    data: EventData,
    related_constructs: Seq[ItemLink],
    related_events: Seq[ItemLink]
)
extends BaseResultValue
with IdValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toBsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(`type`),
                SnakeCaseConstants.Time -> JsonUtils.toBsonValue(time),
                SnakeCaseConstants.Duration -> JsonUtils.toBsonValue(duration),
                SnakeCaseConstants.Message -> JsonUtils.toBsonValue(message),
                SnakeCaseConstants.Origin -> origin.toBsonValue(),
                SnakeCaseConstants.Author -> author.toBsonValue(),
                SnakeCaseConstants.Data -> data.toBsonValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toBsonValue(related_constructs.map(link => link.toBsonValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toBsonValue(related_events.map(link => link.toBsonValue()))
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(`type`),
                SnakeCaseConstants.Time -> JsonUtils.toJsonValue(time),
                SnakeCaseConstants.Duration -> JsonUtils.toJsonValue(duration),
                SnakeCaseConstants.Message -> JsonUtils.toJsonValue(message),
                SnakeCaseConstants.Origin -> origin.toJsValue(),
                SnakeCaseConstants.Author -> author.toJsValue(),
                SnakeCaseConstants.Data -> data.toJsValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toJsonValue(related_constructs.map(link => link.toJsValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toJsonValue(related_events.map(link => link.toJsValue()))
            )
        )
    }
}

object EventResult {
    type CommitEventResult = EventResult[CommitData]
    type PipelineEventResult = EventResult[PipelineData]
    type PipelineJobEventResult = EventResult[PipelineJobData]
    type SubmissionEventResult = EventResult[SubmissionData]

    def fromEvent[EventData <: Data](event: Event, eventData: EventData): EventResult[EventData] = {
        EventResult(
            _id = event.id,
            id = event.id,
            `type` = event.getType,
            time = event.time.toString(),
            duration = event.duration,
            message = event.message,
            origin = event.origin,
            author = event.author,
            data = eventData,
            related_constructs = event.relatedConstructs.map(link => ItemLink.fromLinkTrait(link)),
            related_events = event.relatedEvents.map(link => ItemLink.fromLinkTrait(link))
        )
    }

    def fromCommitSchema(
        commitSchema: CommitSchema,
        pipelineJobIds: Seq[Int],
        gitlabAuthorIds: Seq[String]
    ): CommitEventResult = {
        val commitEvent: CommitEvent = new CommitEvent(commitSchema, pipelineJobIds, gitlabAuthorIds)
        fromEvent(commitEvent, commitEvent.data)
    }

    def fromPipelineSchema(pipelineSchema: PipelineSchema): PipelineEventResult = {
        val pipelineEvent: PipelineEvent = new PipelineEvent(pipelineSchema)
        fromEvent(pipelineEvent, pipelineEvent.data)
    }

    def fromPipelineJobSchema(
        pipelineJobSchema: PipelineJobSchema,
        projectName: String,
        testSuiteNames: Seq[String]
    ): PipelineJobEventResult = {
        val pipelineJobEvent: PipelineJobEvent = new PipelineJobEvent(pipelineJobSchema, projectName, testSuiteNames)
        fromEvent(pipelineJobEvent, pipelineJobEvent.data)
    }

    def fromSubmissionSchema(
        submissionSchema: SubmissionSchema,
        relatedConstructs: Seq[LinkTrait],
        relatedEvents: Seq[LinkTrait]
    ): SubmissionEventResult = {
        val submissionEVent: SubmissionEvent = new SubmissionEvent(submissionSchema, relatedConstructs, relatedEvents)
        fromEvent(submissionEVent, submissionEVent.data)
    }
}
