package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonUtils


final case class CommitOutput(
    hash: String,
    message: String,
    commit_date: String,
    committer_email: String
) {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                Constants.Hash -> JsonUtils.toJsonValue(hash),
                Constants.Message -> JsonUtils.toJsonValue(message),
                Constants.CommitDate -> JsonUtils.toJsonValue(commit_date),
                Constants.CommitterEmail -> JsonUtils.toJsonValue(committer_email)
            )
        )
    }
}
