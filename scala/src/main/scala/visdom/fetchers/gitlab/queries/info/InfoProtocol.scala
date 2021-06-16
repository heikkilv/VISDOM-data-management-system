package visdom.fetchers.gitlab.queries.info

import spray.json.RootJsonFormat
import visdom.fetchers.gitlab.queries.GitlabProtocol
import visdom.fetchers.gitlab.queries.GitlabResponseAccepted


trait InfoProtocol extends GitlabProtocol {
    implicit val infoResponseFormat: RootJsonFormat[InfoResponse] =
        jsonFormat9(InfoResponse)
}
