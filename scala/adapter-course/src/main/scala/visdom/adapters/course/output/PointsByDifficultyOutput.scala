package visdom.adapters.course.output

import spray.json.JsObject
import visdom.adapters.course.schemas.ModulePointDifficultySchema
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.CommonConstants
import visdom.utils.PascalCaseConstants


final case class PointsByDifficultyOutput(
    empty: Option[Int],
    g: Option[Int],
    p: Option[Int]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            CommonConstants.EmptyString -> JsonUtils.toJsonValue(empty),
            PascalCaseConstants.G -> JsonUtils.toJsonValue(g),
            PascalCaseConstants.P -> JsonUtils.toJsonValue(p)
        )
    }
}

object PointsByDifficultyOutput {
    def fromPointDifficultySchema(pointDifficultSchema: ModulePointDifficultySchema): PointsByDifficultyOutput = {
        PointsByDifficultyOutput(
            empty = pointDifficultSchema.empty,
            g = pointDifficultSchema.G,
            p = pointDifficultSchema.P
        )
    }
}
