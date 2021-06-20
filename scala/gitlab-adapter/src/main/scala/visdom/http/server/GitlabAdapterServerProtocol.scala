package visdom.http.server

import spray.json.RootJsonFormat


trait GitlabAdapterServerProtocol
extends ServerProtocol {
    implicit lazy val gitlabAdapterInfoResponseFormat: RootJsonFormat[response.GitlabAdapterInfoResponse] =
        jsonFormat7(response.GitlabAdapterInfoResponse)
}
