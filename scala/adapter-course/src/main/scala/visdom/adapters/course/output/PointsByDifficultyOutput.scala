package visdom.adapters.course.output

import spray.json.JsObject
import visdom.adapters.course.schemas.ModulePointDifficultySchema
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.CommonConstants
import visdom.utils.PascalCaseConstants
import visdom.utils.SnakeCaseConstants


final case class PointsByDifficultyOutput(
    empty: Option[Int],
    G: Option[Int],
    P: Option[Int]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.Empty -> JsonUtils.toJsonValue(empty),
            PascalCaseConstants.G -> JsonUtils.toJsonValue(G),
            PascalCaseConstants.P -> JsonUtils.toJsonValue(P)
        )
    }
}

object PointsByDifficultyOutput {
    def fromPointDifficultySchema(pointDifficultSchema: ModulePointDifficultySchema): PointsByDifficultyOutput = {
        PointsByDifficultyOutput(
            empty = pointDifficultSchema.empty,
            G = pointDifficultSchema.G,
            P = pointDifficultSchema.P
        )
    }
}
