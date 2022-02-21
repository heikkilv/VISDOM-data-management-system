package visdom.adapters.general.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonNull
import org.mongodb.scala.bson.BsonValue
import spray.json.JsNull
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.PipelineLinksSchema
import visdom.adapters.general.schemas.PipelineSchema
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class PipelineData(
    pipeline_id: Int,
    project_id: Int,
    sha: String,
    ref: String,
    source: String,
    created_at: String,
    updated_at: String,
    finished_at: String,
    queued_duration: Double,
    tag: Boolean,
    detailed_status: PipelineStatus,
    jobs: Seq[Int]
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.PipelineId -> JsonUtils.toBsonValue(pipeline_id),
                SnakeCaseConstants.ProjectId -> JsonUtils.toBsonValue(project_id),
                SnakeCaseConstants.Sha -> JsonUtils.toBsonValue(sha),
                SnakeCaseConstants.Ref -> JsonUtils.toBsonValue(ref),
                SnakeCaseConstants.Source -> JsonUtils.toBsonValue(source),
                SnakeCaseConstants.CreatedAt -> JsonUtils.toBsonValue(created_at),
                SnakeCaseConstants.UpdatedAt -> JsonUtils.toBsonValue(updated_at),
                SnakeCaseConstants.FinishedAt -> JsonUtils.toBsonValue(finished_at),
                SnakeCaseConstants.QueuedDuration -> JsonUtils.toBsonValue(queued_duration),
                SnakeCaseConstants.Tag -> JsonUtils.toBsonValue(tag),
                SnakeCaseConstants.DetailedStatus -> detailed_status.toBsonValue(),
                SnakeCaseConstants.Jobs -> JsonUtils.toBsonValue(jobs)
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.PipelineId -> JsonUtils.toJsonValue(pipeline_id),
                SnakeCaseConstants.ProjectId -> JsonUtils.toJsonValue(project_id),
                SnakeCaseConstants.Sha -> JsonUtils.toJsonValue(sha),
                SnakeCaseConstants.Ref -> JsonUtils.toJsonValue(ref),
                SnakeCaseConstants.Source -> JsonUtils.toJsonValue(source),
                SnakeCaseConstants.CreatedAt -> JsonUtils.toJsonValue(created_at),
                SnakeCaseConstants.UpdatedAt -> JsonUtils.toJsonValue(updated_at),
                SnakeCaseConstants.FinishedAt -> JsonUtils.toJsonValue(finished_at),
                SnakeCaseConstants.QueuedDuration -> JsonUtils.toJsonValue(queued_duration),
                SnakeCaseConstants.Tag -> JsonUtils.toJsonValue(tag),
                SnakeCaseConstants.DetailedStatus -> detailed_status.toJsValue(),
                SnakeCaseConstants.Jobs -> JsonUtils.toJsonValue(jobs)
            )
        )
    }
}

object PipelineData {
    def fromPipelineSchema(pipelineSchema: PipelineSchema): PipelineData = {
        PipelineData(
            pipeline_id = pipelineSchema.id,
            project_id = pipelineSchema.project_id,
            sha = pipelineSchema.sha,
            ref = pipelineSchema.ref,
            source = pipelineSchema.source,
            created_at = pipelineSchema.created_at,
            updated_at = pipelineSchema.updated_at,
            finished_at = pipelineSchema.finished_at,
            queued_duration = pipelineSchema.queued_duration,
            tag = pipelineSchema.tag,
            detailed_status = PipelineStatus(
                text = pipelineSchema.detailed_status.text,
                label = pipelineSchema.detailed_status.label,
                group = pipelineSchema.detailed_status.group
            ),
            jobs = pipelineSchema._links match {
                case Some(links: PipelineLinksSchema) => links.jobs match {
                    case Some(jobIds: Seq[Int]) => jobIds
                    case None => Seq.empty
                }
                case None => Seq.empty
            }
        )
    }
}
