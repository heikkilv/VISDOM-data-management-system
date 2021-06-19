package visdom.adapter.gitlab.results

import spray.json.JsNumber
import spray.json.JsValue


final case class CommitResult(
    project_name: String,
    committer_name: String,
    date: String,
    count: Long
) {
    def toJsonTuple(): (String, String, String, JsValue) = {
        (
            project_name,
            committer_name,
            date,
            JsNumber(count)
        )
    }
}
