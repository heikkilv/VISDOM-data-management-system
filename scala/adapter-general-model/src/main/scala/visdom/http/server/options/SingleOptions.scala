package visdom.http.server.options

import visdom.adapters.options.SingleQueryOptions


final case class SingleOptions(
    objectType: String,
    uuid: String
)
extends BaseInputOptions {
    def toQueryOptions(): SingleQueryOptions = {
        SingleQueryOptions(
            objectType = objectType,
            uuid = uuid
        )
    }
}
