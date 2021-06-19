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
    implicit lazy val allDataOptionsFormat: RootJsonFormat[fetcher.gitlab.AllDataQueryOptions] =
        jsonFormat4(fetcher.gitlab.AllDataQueryOptions)
    implicit lazy val commitQueryOptionsFormat: RootJsonFormat[fetcher.gitlab.CommitQueryOptions] =
        jsonFormat8(fetcher.gitlab.CommitQueryOptions)
    implicit lazy val fileOptionsFormat: RootJsonFormat[fetcher.gitlab.FileQueryOptions] =
        jsonFormat5(fetcher.gitlab.FileQueryOptions)

    implicit lazy val responseProblemFormat: RootJsonFormat[response.ResponseProblem] =
        jsonFormat2(response.ResponseProblem)

    implicit lazy val responseAcceptedFormat: RootJsonFormat[response.ResponseAccepted] =
        jsonFormat3(response.ResponseAccepted)

    implicit lazy val brokerInfoResponseFormat: RootJsonFormat[response.BrokerInfoResponse] =
        jsonFormat6(response.BrokerInfoResponse)
    implicit lazy val gitlabFetcherInfoResponseFormat: RootJsonFormat[response.GitlabFetcherInfoResponse] =
        jsonFormat9(response.GitlabFetcherInfoResponse)
    implicit lazy val gitlabAdapterInfoResponseFormat: RootJsonFormat[response.GitlabAdapterInfoResponse] =
        jsonFormat7(response.GitlabAdapterInfoResponse)

    implicit val timeout: Timeout = Timeout((ServerConstants.DefaultMaxResponseDelaySeconds + 1).seconds)
}
