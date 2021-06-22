package visdom.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.util.Timeout
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat


trait ServerProtocol
extends DefaultJsonProtocol
with SprayJsonSupport {
    implicit lazy val responseProblemFormat: RootJsonFormat[response.ResponseProblem] =
        jsonFormat2(response.ResponseProblem)
    implicit lazy val responseAcceptedFormat: RootJsonFormat[response.ResponseAccepted] =
        jsonFormat3(response.ResponseAccepted)

    implicit val timeout: Timeout = Timeout((ServerConstants.DefaultMaxResponseDelaySeconds + 1).seconds)
}
