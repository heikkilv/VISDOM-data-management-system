package visdom.http.server.options

import visdom.adapters.options.MultiQueryOptions
import visdom.adapters.options.ObjectTypes
import visdom.http.server.services.constants.GeneralAdapterConstants
import visdom.utils.CommonConstants


final case class MultiOptions(
    pageOptions: OnlyPageInputOptions,
    targetType: String,
    objectType: String,
    dataAttributes: Option[String],
    includedLinks: String
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
            dataAttributes = dataAttributes.map(attributesString => attributesString.split(CommonConstants.Comma)),
            includedLinks = includedLinks match {
                case GeneralAdapterConstants.None => LinksNone
                case GeneralAdapterConstants.Constructs => LinksConstructs
                case GeneralAdapterConstants.Events => LinksEvents
                case _ => LinksAll
            },
            page = queryPageOptions.page,
            pageSize = queryPageOptions.pageSize
        )
    }
}
