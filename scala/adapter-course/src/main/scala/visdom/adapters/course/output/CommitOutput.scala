package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class CommitOutput(
    hash: String,
    message: String,
    commit_date: String,
    committer_email: String
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.Hash -> JsonUtils.toJsonValue(hash),
            SnakeCaseConstants.Message -> JsonUtils.toJsonValue(message),
            SnakeCaseConstants.CommitDate -> JsonUtils.toJsonValue(commit_date),
            SnakeCaseConstants.CommitterEmail -> JsonUtils.toJsonValue(committer_email)
        )
    }
}
