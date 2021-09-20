package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ModuleCommitsOutput(
    module_name: String,
    projects: Seq[ExerciseCommitsOutput]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.ModuleName -> JsonUtils.toJsonValue(module_name),
            SnakeCaseConstants.Projects -> JsonUtils.toJsonValue(projects)
        )
    }
}
