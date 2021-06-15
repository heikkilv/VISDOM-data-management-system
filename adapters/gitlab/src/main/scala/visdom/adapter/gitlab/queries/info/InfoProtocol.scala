package visdom.adapter.gitlab.queries.info

import spray.json.RootJsonFormat
import visdom.adapter.gitlab.queries.GitlabProtocol


trait InfoProtocol extends GitlabProtocol {
    implicit val infoResponseFormat: RootJsonFormat[InfoResponse] =
        jsonFormat5(InfoResponse)
}
