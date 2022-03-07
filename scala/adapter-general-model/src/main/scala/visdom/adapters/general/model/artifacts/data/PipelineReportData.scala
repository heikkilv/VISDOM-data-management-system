package visdom.adapters.general.model.artifacts.data


import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.PipelineReportSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class PipelineReportData(
    total_time: Double,
    total_count: Int,
    success_count: Int,
    failed_count: Int,
    skipped_count: Int,
    error_count: Int
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.TotalTime -> JsonUtils.toBsonValue(total_time),
                SnakeCaseConstants.TotalCount -> JsonUtils.toBsonValue(total_count),
                SnakeCaseConstants.SuccessCount -> JsonUtils.toBsonValue(success_count),
                SnakeCaseConstants.FailedCount -> JsonUtils.toBsonValue(failed_count),
                SnakeCaseConstants.SkippedCount -> JsonUtils.toBsonValue(skipped_count),
                SnakeCaseConstants.ErrorCount -> JsonUtils.toBsonValue(error_count)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.TotalTime -> JsonUtils.toJsonValue(total_time),
                SnakeCaseConstants.TotalCount -> JsonUtils.toJsonValue(total_count),
                SnakeCaseConstants.SuccessCount -> JsonUtils.toJsonValue(success_count),
                SnakeCaseConstants.FailedCount -> JsonUtils.toJsonValue(failed_count),
                SnakeCaseConstants.SkippedCount -> JsonUtils.toJsonValue(skipped_count),
                SnakeCaseConstants.ErrorCount -> JsonUtils.toJsonValue(error_count)
            )
        )
    }
}

object PipelineReportData {
    def fromPipelineReportSchema(pipelineReportSchema: PipelineReportSchema): PipelineReportData = {
        PipelineReportData(
            total_time = pipelineReportSchema.total_time,
            total_count = pipelineReportSchema.total_count,
            success_count = pipelineReportSchema.success_count,
            failed_count = pipelineReportSchema.failed_count,
            skipped_count = pipelineReportSchema.skipped_count,
            error_count = pipelineReportSchema.error_count
        )
    }
}
