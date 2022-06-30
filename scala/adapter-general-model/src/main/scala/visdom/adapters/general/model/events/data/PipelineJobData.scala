// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.schemas.PipelineJobSchema
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class PipelineJobData(
    job_id: Int,
    pipeline_id: Int,
    user_id: Int,
    commit_id: String,
    stage: String,
    name: String,
    ref: String,
    tag: Boolean,
    created_at: String,
    finished_at: Option[String],
    queued_duration: Double
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.JobId -> JsonUtils.toBsonValue(job_id),
                SnakeCaseConstants.PipelineId -> JsonUtils.toBsonValue(pipeline_id),
                SnakeCaseConstants.UserId -> JsonUtils.toBsonValue(user_id),
                SnakeCaseConstants.CommitId -> JsonUtils.toBsonValue(commit_id),
                SnakeCaseConstants.Stage -> JsonUtils.toBsonValue(stage),
                SnakeCaseConstants.Name -> JsonUtils.toBsonValue(name),
                SnakeCaseConstants.Ref -> JsonUtils.toBsonValue(ref),
                SnakeCaseConstants.Tag -> JsonUtils.toBsonValue(tag),
                SnakeCaseConstants.CreatedAt -> JsonUtils.toBsonValue(created_at),
                SnakeCaseConstants.FinishedAt -> JsonUtils.toBsonValue(finished_at),
                SnakeCaseConstants.QueuedDuration -> JsonUtils.toBsonValue(queued_duration)
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.JobId -> JsonUtils.toJsonValue(job_id),
                SnakeCaseConstants.PipelineId -> JsonUtils.toJsonValue(pipeline_id),
                SnakeCaseConstants.UserId -> JsonUtils.toJsonValue(user_id),
                SnakeCaseConstants.CommitId -> JsonUtils.toJsonValue(commit_id),
                SnakeCaseConstants.Stage -> JsonUtils.toJsonValue(stage),
                SnakeCaseConstants.Name -> JsonUtils.toJsonValue(name),
                SnakeCaseConstants.Ref -> JsonUtils.toJsonValue(ref),
                SnakeCaseConstants.Tag -> JsonUtils.toJsonValue(tag),
                SnakeCaseConstants.CreatedAt -> JsonUtils.toJsonValue(created_at),
                SnakeCaseConstants.FinishedAt -> JsonUtils.toJsonValue(finished_at),
                SnakeCaseConstants.QueuedDuration -> JsonUtils.toJsonValue(queued_duration)
            )
        )
    }
}

object PipelineJobData {
    def fromPipelineJobSchema(pipelineJobSchema: PipelineJobSchema): PipelineJobData = {
        PipelineJobData(
            job_id = pipelineJobSchema.id,
            pipeline_id = pipelineJobSchema.pipeline.id,
            user_id = pipelineJobSchema.user.id,
            commit_id = pipelineJobSchema.commit.id,
            stage = pipelineJobSchema.stage,
            name = pipelineJobSchema.name,
            ref = pipelineJobSchema.ref,
            tag = pipelineJobSchema.tag,
            created_at = pipelineJobSchema.created_at,
            finished_at = pipelineJobSchema.finished_at,
            queued_duration = pipelineJobSchema.queued_duration.getOrElse(0.0)
        )
    }
}
