package visdom.adapters.general.model.metadata.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.ModuleAdditionalSchema
import visdom.adapters.general.schemas.ModuleLinksSchema
import visdom.adapters.general.schemas.ModuleNameSchema
import visdom.adapters.general.schemas.ModuleSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils


final case class ModuleData(
    module_id: Int,
    module_number: Int,
    url: String,
    html_url: String,
    is_open: Boolean,
    start_date: Option[String],
    end_date: Option[String],
    late_submission_date: Option[String],
    max_points: Int,
    points_to_pass: Int,
    course_id: Int,
    exercises: Seq[Int]
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.ModuleId -> JsonUtils.toBsonValue(module_id),
                SnakeCaseConstants.ModuleNumber -> JsonUtils.toBsonValue(module_number),
                SnakeCaseConstants.Url -> JsonUtils.toBsonValue(url),
                SnakeCaseConstants.HtmlUrl -> JsonUtils.toBsonValue(html_url),
                SnakeCaseConstants.IsOpen -> JsonUtils.toBsonValue(is_open),
                SnakeCaseConstants.StartDate -> JsonUtils.toBsonValue(start_date),
                SnakeCaseConstants.EndDate -> JsonUtils.toBsonValue(end_date),
                SnakeCaseConstants.LateSubmissionDate -> JsonUtils.toBsonValue(late_submission_date),
                SnakeCaseConstants.MaxPoints -> JsonUtils.toBsonValue(max_points),
                SnakeCaseConstants.PointsToPass -> JsonUtils.toBsonValue(points_to_pass),
                SnakeCaseConstants.CourseId -> JsonUtils.toBsonValue(course_id),
                SnakeCaseConstants.Exercises -> JsonUtils.toBsonValue(exercises)
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.ModuleId -> JsonUtils.toJsonValue(module_id),
                SnakeCaseConstants.ModuleNumber -> JsonUtils.toJsonValue(module_number),
                SnakeCaseConstants.Url -> JsonUtils.toJsonValue(url),
                SnakeCaseConstants.HtmlUrl -> JsonUtils.toJsonValue(html_url),
                SnakeCaseConstants.IsOpen -> JsonUtils.toJsonValue(is_open),
                SnakeCaseConstants.StartDate -> JsonUtils.toJsonValue(start_date),
                SnakeCaseConstants.EndDate -> JsonUtils.toJsonValue(end_date),
                SnakeCaseConstants.LateSubmissionDate -> JsonUtils.toJsonValue(late_submission_date),
                SnakeCaseConstants.MaxPoints -> JsonUtils.toJsonValue(max_points),
                SnakeCaseConstants.PointsToPass -> JsonUtils.toJsonValue(points_to_pass),
                SnakeCaseConstants.CourseId -> JsonUtils.toJsonValue(course_id),
                SnakeCaseConstants.Exercises -> JsonUtils.toJsonValue(exercises)
            )
        )
    }
}

object ModuleData {
    def fromModuleSchema(moduleSchema: ModuleSchema, additionalSchema: ModuleAdditionalSchema): ModuleData = {
        ModuleData(
            module_id = moduleSchema.id,
            module_number = getModuleNumber(moduleSchema.display_name),
            url = moduleSchema.url,
            html_url = moduleSchema.html_url,
            is_open = moduleSchema.is_open,
            start_date =
                moduleSchema.metadata.other.map(other => ExerciseData.dateStringToIsoFormat(other.start_date)),
            end_date =
                moduleSchema.metadata.other.map(other => ExerciseData.dateStringToIsoFormat(other.end_date)),
            late_submission_date =
                moduleSchema.metadata.other.map(other => other.late_submission_date).flatten
                    .map(date => ExerciseData.dateStringToIsoFormat(date)),
            max_points = additionalSchema.max_points,
            points_to_pass = additionalSchema.points_to_pass,
            course_id = moduleSchema.course_id,
            exercises = moduleSchema._links.map(links => links.exercises).flatten.getOrElse(Seq.empty)
        )
    }

    def getModuleNumber(displayName: ModuleNameSchema): Int = {
        def getNumberFromEnglishName(): Option[Int] = {
            displayName.en match {
                case Some(englishName: String) => englishName.split(CommonConstants.LiteralDot).headOption match {
                    case Some(numberPart: String) => GeneralUtils.toIntOption(numberPart)
                    case None => None
                }
                case None => None
            }
        }

        getNumberFromEnglishName() match {
            case Some(englishNumber: Int) => englishNumber
            case None => displayName.number match {
                case Some(moduleNumberString: String) => GeneralUtils.toIntOption(
                    moduleNumberString.replace(CommonConstants.Dot, CommonConstants.EmptyString)
                ) match {
                    case Some(moduleNumber: Int) => moduleNumber
                    case None => 0
                }
                case None => 0
            }
        }
    }
}
