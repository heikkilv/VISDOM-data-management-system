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
