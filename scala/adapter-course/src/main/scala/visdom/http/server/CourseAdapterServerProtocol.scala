package visdom.http.server

import akka.util.Timeout
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import spray.json.RootJsonFormat


trait CourseAdapterServerProtocol
extends ServerProtocol {
    implicit lazy val courseAdapterInfoResponseFormat: RootJsonFormat[response.CourseAdapterInfoResponse] =
        jsonFormat7(response.CourseAdapterInfoResponse)

    override implicit val timeout: Timeout = Timeout((10 * ServerConstants.DefaultMaxResponseDelaySeconds + 1).seconds)
}
