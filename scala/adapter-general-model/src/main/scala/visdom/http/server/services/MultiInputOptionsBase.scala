package visdom.http.server.services

import visdom.http.server.options.MultiOptions
import visdom.http.server.options.OnlyPageInputOptions
import visdom.adapters.options.ObjectTypes


trait MultiInputOptionsBase extends MultiInputOptionsTrait {
    def getMultiOptions(
        targetType: String,
        page: Option[String],
        pageSize: Option[String],
        objectType: String,
        query: Option[String],
        dataAttributes: Option[String],
        includedLInks: String
    ): MultiOptions = {
        MultiOptions(
            pageOptions = OnlyPageInputOptions(
                page = page,
                pageSize = pageSize
            ),
            targetType = targetType,
            objectType = objectType,
            query = query,
            dataAttributes = dataAttributes,
            includedLinks = includedLInks
        )
    }
}
