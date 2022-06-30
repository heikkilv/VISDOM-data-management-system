// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.options

import visdom.adapters.options.QueryWithPageAndTokenOptions


final case class PageWithTokenOptions(
    pageOptions: OnlyPageInputOptions,
    token: Option[String]
)
extends BaseInputOptions {
    def toQueryOptions(): QueryWithPageAndTokenOptions = {
        val queryPageOptions = pageOptions.toOnlyPageOptions()
        QueryWithPageAndTokenOptions(
            page = queryPageOptions.page,
            pageSize = queryPageOptions.pageSize,
            token = token
        )
    }
}
