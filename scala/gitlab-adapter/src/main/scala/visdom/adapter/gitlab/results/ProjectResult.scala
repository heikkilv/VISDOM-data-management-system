package visdom.adapter.gitlab.results

import spray.json.JsValue
import visdom.json.JsonUtils


final case class ProjectResult(
    group_name: String,
    project_name: String
)
