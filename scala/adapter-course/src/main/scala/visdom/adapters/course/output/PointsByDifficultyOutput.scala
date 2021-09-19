package visdom.adapters.course.output

import spray.json.JsObject
import visdom.adapters.course.schemas.ModulePointDifficultySchema
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedJsObject
import visdom.utils.CommonConstants
import visdom.utils.PascalCaseConstants


final case class PointsByDifficultyOutput(
    category: Option[Int],
    categoryG: Option[Int],
    categoryP: Option[Int]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            CommonConstants.EmptyString -> JsonUtils.toJsonValue(category),
            PascalCaseConstants.G -> JsonUtils.toJsonValue(categoryG),
            PascalCaseConstants.P -> JsonUtils.toJsonValue(categoryP)
        )
        .removeNulls()
    }
}

object PointsByDifficultyOutput {
    def fromPointDifficultySchema(pointDifficultSchema: ModulePointDifficultySchema): PointsByDifficultyOutput = {
        PointsByDifficultyOutput(
            category = pointDifficultSchema.category,
            categoryG = pointDifficultSchema.categoryG,
            categoryP = pointDifficultSchema.categoryP
        )
    }
}
