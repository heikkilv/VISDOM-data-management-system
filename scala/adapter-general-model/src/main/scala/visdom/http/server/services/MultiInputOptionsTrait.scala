package visdom.http.server.services

import visdom.http.server.QueryOptionsBase


trait MultiInputOptionsTrait {
    def getMultiOptions(
        targetType: String,
        page: Option[String],
        pageSize: Option[String],
        objectType: String,
        query: Option[String],
        dataAttributes: Option[String],
        includedLInks: String
    ): QueryOptionsBase
}
