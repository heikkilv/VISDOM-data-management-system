package visdom.adapters.course.output

import spray.json.JsObject
import visdom.adapters.course.schemas.SubmissionPointsSchema
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class SubmissionPointsOutput(
    id: Int,
    submission_time: String,
    grade: Int
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
            SnakeCaseConstants.SubmissionTime -> JsonUtils.toJsonValue(submission_time),
            SnakeCaseConstants.Grade -> JsonUtils.toJsonValue(grade)
        )
    }
}

object SubmissionPointsOutput {
    def fromSubmissionPointsSchema(submissionPointsSchema: SubmissionPointsSchema): SubmissionPointsOutput = {
        SubmissionPointsOutput(
            id = submissionPointsSchema.id,
            submission_time = submissionPointsSchema.submission_time,
            grade = submissionPointsSchema.grade
        )
    }
}
