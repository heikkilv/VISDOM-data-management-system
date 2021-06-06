package visdom.fetchers.gitlab.queries.commits

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat


trait CommitProtocol
extends DefaultJsonProtocol
with SprayJsonSupport {
    implicit val commitOptionsFormat: RootJsonFormat[CommitQueryOptions] =
        jsonFormat8(CommitQueryOptions)
    implicit val commitResponseAcceptedFormat: RootJsonFormat[CommitResponseAccepted] =
        jsonFormat3(CommitResponseAccepted)
    implicit val commitResponseInvalidFormat: RootJsonFormat[CommitResponseInvalid] =
        jsonFormat2(CommitResponseInvalid)
    implicit val commitResponseUnauthorizedFormat: RootJsonFormat[CommitResponseUnauthorized] =
        jsonFormat2(CommitResponseUnauthorized)
    implicit val commitResponseNotFoundFormat: RootJsonFormat[CommitResponseNotFound] =
        jsonFormat2(CommitResponseNotFound)
    implicit val commitResponseErrorFormat: RootJsonFormat[CommitResponseError] =
        jsonFormat2(CommitResponseError)

    implicit val timeout: Timeout = Timeout(11.seconds)
    val maxWaitTime: Duration = Duration(10, TimeUnit.SECONDS)
}
