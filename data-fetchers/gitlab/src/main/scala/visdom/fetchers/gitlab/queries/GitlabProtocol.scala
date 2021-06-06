package visdom.fetchers.gitlab.queries

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.util.Timeout
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat
import visdom.fetchers.gitlab.GitlabConstants



trait GitlabProtocol
extends DefaultJsonProtocol
with SprayJsonSupport {
    implicit val gitlabResponseProblemFormat: RootJsonFormat[GitlabResponseProblem] =
        jsonFormat2(GitlabResponseProblem)

    implicit val timeout: Timeout = Timeout(11.seconds)
    val maxWaitTime: Duration = GitlabConstants.DefaultWaitDuration
}
