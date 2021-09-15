package visdom.adapters.course.output

import spray.json.JsArray
import spray.json.JsObject
import visdom.json.JsonUtils


final case class ExerciseCommitsOutput(
    name: String,
    commit_count: Int,
    commit_meta: Seq[CommitOutput]
) {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                Constants.Name -> JsonUtils.toJsonValue(name),
                Constants.CommitCount -> JsonUtils.toJsonValue(commit_count),
                Constants.CommitMeta -> JsArray(commit_meta.map(commit => commit.toJsObject()).toList)
            )
        )
    }
}
