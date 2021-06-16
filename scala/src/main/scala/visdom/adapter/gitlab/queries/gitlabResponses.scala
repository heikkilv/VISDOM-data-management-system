package visdom.adapter.gitlab.queries

import spray.json.JsObject


abstract class GitlabResponse

final case class GitlabResponseOk(
    data: JsObject
) extends GitlabResponse

final case class GitlabResponseProblem(
    status: String,
    description: String
)
extends GitlabResponse
