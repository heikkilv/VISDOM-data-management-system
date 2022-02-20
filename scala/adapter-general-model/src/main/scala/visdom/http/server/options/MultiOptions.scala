package visdom.http.server.options

import visdom.adapters.options.MultiQueryOptions
import visdom.adapters.options.ObjectTypes


final case class MultiOptions(
    pageOptions: OnlyPageInputOptions,
    targetType: String,
    objectType: String
)
extends BaseInputOptions {
    def toQueryOptions(): MultiQueryOptions = {
        val queryPageOptions = pageOptions.toOnlyPageOptions()
        MultiQueryOptions(
            targetType = ObjectTypes.objectTypes.contains(targetType) match {
                case true => targetType
                case false => ObjectTypes.TargetTypeOrigin
            },
            objectType = objectType.isEmpty() match {
                case true => None
                case false => Some(objectType)
            },
            page = queryPageOptions.page,
            pageSize = queryPageOptions.pageSize
        )
    }
}
