package visdom.adapters.general.model.artifacts.data


import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.TestCaseSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class TestCaseData(
    execution_time: Double,
    classname: String,
    file: Option[String],
    system_output: Option[String],
    stack_trace: Option[String],
    recent_failures: Option[String],
    pipeline_id: Int,
    test_suite: String
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.ExecutionTime -> JsonUtils.toBsonValue(execution_time),
                SnakeCaseConstants.Classname -> JsonUtils.toBsonValue(classname),
                SnakeCaseConstants.File -> JsonUtils.toBsonValue(file),
                SnakeCaseConstants.SystemOutput -> JsonUtils.toBsonValue(system_output),
                SnakeCaseConstants.StackTrace -> JsonUtils.toBsonValue(stack_trace),
                SnakeCaseConstants.RecentFailures -> JsonUtils.toBsonValue(recent_failures),
                SnakeCaseConstants.PipelineId -> JsonUtils.toBsonValue(pipeline_id),
                SnakeCaseConstants.TestSuite -> JsonUtils.toBsonValue(test_suite)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.ExecutionTime -> JsonUtils.toJsonValue(execution_time),
                SnakeCaseConstants.Classname -> JsonUtils.toJsonValue(classname),
                SnakeCaseConstants.File -> JsonUtils.toJsonValue(file),
                SnakeCaseConstants.SystemOutput -> JsonUtils.toJsonValue(system_output),
                SnakeCaseConstants.StackTrace -> JsonUtils.toJsonValue(stack_trace),
                SnakeCaseConstants.RecentFailures -> JsonUtils.toJsonValue(recent_failures),
                SnakeCaseConstants.PipelineId -> JsonUtils.toJsonValue(pipeline_id),
                SnakeCaseConstants.TestSuite -> JsonUtils.toJsonValue(test_suite)
            )
        )
    }
}

object TestCaseData {
    def fromTestCaseSchema(testCaseSchema: TestCaseSchema, pipelineId: Int, testSuiteName: String): TestCaseData = {
        TestCaseData(
            execution_time = testCaseSchema.execution_time,
            classname = testCaseSchema.classname,
            file = testCaseSchema.file,
            system_output = testCaseSchema.system_output,
            stack_trace = testCaseSchema.stack_trace,
            recent_failures = testCaseSchema.recent_failures,
            pipeline_id = pipelineId,
            test_suite = testSuiteName
        )
    }
}
