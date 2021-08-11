package visdom.http.server

import spray.json.RootJsonFormat


trait GitlabFetcherServerProtocol
extends ServerProtocol {
    implicit lazy val gitlabAdapterInfoResponseFormat: RootJsonFormat[response.GitlabFetcherInfoResponse] =
        jsonFormat9(response.GitlabFetcherInfoResponse)

    implicit lazy val allDataOptionsFormat: RootJsonFormat[fetcher.gitlab.AllDataQueryOptions] =
        jsonFormat5(fetcher.gitlab.AllDataQueryOptions)
    implicit lazy val commitQueryOptionsFormat: RootJsonFormat[fetcher.gitlab.CommitQueryOptions] =
        jsonFormat9(fetcher.gitlab.CommitQueryOptions)
    implicit lazy val fileOptionsFormat: RootJsonFormat[fetcher.gitlab.FileQueryOptions] =
        jsonFormat6(fetcher.gitlab.FileQueryOptions)
}
