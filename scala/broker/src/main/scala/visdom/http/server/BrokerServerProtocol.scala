package visdom.http.server

import spray.json.RootJsonFormat


trait BrokerServerProtocol
extends ServerProtocol {
    implicit lazy val brokerInfoResponseFormat: RootJsonFormat[response.BrokerInfoResponse] =
        jsonFormat6(response.BrokerInfoResponse)
}
