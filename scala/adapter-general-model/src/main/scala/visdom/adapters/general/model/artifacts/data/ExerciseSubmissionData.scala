package visdom.adapters.general.model.artifacts.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ExerciseSubmissionData(
    id: Int,
    grade: Int,
    submission_time: String
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toBsonValue(id),
                SnakeCaseConstants.Grade -> JsonUtils.toBsonValue(grade),
                SnakeCaseConstants.SubmissionTime -> JsonUtils.toBsonValue(submission_time)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
                SnakeCaseConstants.Grade -> JsonUtils.toJsonValue(grade),
                SnakeCaseConstants.SubmissionTime -> JsonUtils.toJsonValue(submission_time)
            )
        )
    }
}
