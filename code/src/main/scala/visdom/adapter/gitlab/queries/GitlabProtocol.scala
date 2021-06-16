package visdom.adapter.gitlab.queries

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.util.Timeout
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat


trait GitlabProtocol
extends DefaultJsonProtocol
with SprayJsonSupport {
    implicit val gitlabResponseProblemFormat: RootJsonFormat[GitlabResponseProblem] =
        jsonFormat2(GitlabResponseProblem)

    implicit val timeout: Timeout = Timeout((Constants.DefaultWaitDurationSeconds + 1).seconds)
    val maxWaitTime: Duration = Constants.DefaultWaitDuration
}
