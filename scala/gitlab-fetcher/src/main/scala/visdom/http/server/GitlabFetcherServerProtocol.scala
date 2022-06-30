// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
