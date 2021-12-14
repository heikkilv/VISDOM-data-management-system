package visdom.http.server

import akka.util.Timeout
import scala.concurrent.duration.DurationInt
import spray.json.RootJsonFormat


trait AdapterServerProtocol
extends ServerProtocol {
    implicit lazy val infoResponseFormat: RootJsonFormat[response.InfoResponse] =
        jsonFormat7(response.InfoResponse)

    override implicit val timeout: Timeout = Timeout((10 * ServerConstants.DefaultMaxResponseDelaySeconds + 1).seconds)
}
