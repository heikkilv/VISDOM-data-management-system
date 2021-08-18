package visdom.http.server

import spray.json.RootJsonFormat


trait APlusFetcherServerProtocol
extends ServerProtocol {
    implicit lazy val aPlusFetcherInfoResponseFormat: RootJsonFormat[response.APlusFetcherInfoResponse] =
        jsonFormat9(response.APlusFetcherInfoResponse)
}
