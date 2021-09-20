package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ExerciseCommitsOutput(
    name: String,
    commit_count: Int,
    commit_meta: Seq[CommitOutput]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.Name -> JsonUtils.toJsonValue(name),
            SnakeCaseConstants.CommitCount -> JsonUtils.toJsonValue(commit_count),
            SnakeCaseConstants.CommitMeta -> JsonUtils.toJsonValue(commit_meta)
        )
    }
}
