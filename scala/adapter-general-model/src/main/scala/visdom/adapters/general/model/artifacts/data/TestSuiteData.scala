package visdom.adapters.general.model.artifacts.data


import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.TestSuiteSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class TestSuiteData(
    total_time: Double,
    total_count: Int,
    success_count: Int,
    failed_count: Int,
    skipped_count: Int,
    error_count: Int,
    suite_error: Option[String]
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
                SnakeCaseConstants.ErrorCount -> JsonUtils.toBsonValue(error_count),
                SnakeCaseConstants.SuiteError -> JsonUtils.toBsonValue(suite_error)
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
                SnakeCaseConstants.ErrorCount -> JsonUtils.toJsonValue(error_count),
                SnakeCaseConstants.SuiteError -> JsonUtils.toJsonValue(suite_error)
            )
        )
    }
}

object TestSuiteData {
    def fromTestSuiteSchema(testSuiteSchema: TestSuiteSchema): TestSuiteData = {
        TestSuiteData(
            total_time = testSuiteSchema.total_time,
            total_count = testSuiteSchema.total_count,
            success_count = testSuiteSchema.success_count,
            failed_count = testSuiteSchema.failed_count,
            skipped_count = testSuiteSchema.skipped_count,
            error_count = testSuiteSchema.error_count,
            suite_error = testSuiteSchema.suite_error
        )
    }
}
