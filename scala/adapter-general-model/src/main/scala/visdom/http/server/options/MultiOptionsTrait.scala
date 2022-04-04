package visdom.http.server.options

import visdom.adapters.options.AttributeFilterTrait
import visdom.adapters.options.MultiQueryOptions
import visdom.adapters.options.ObjectTypes
import visdom.http.server.services.constants.GeneralAdapterConstants
import visdom.utils.CommonConstants


trait MultiOptionsTrait {
    val pageOptions: OnlyPageInputOptions
    val targetType: String
    val objectType: String
    val query: Option[String]
    val dataAttributes: Option[String]
    val includedLinks: String

    def getObjectTypes(): Map[String, Set[String]]

    def getAttributeFilters(): Option[Seq[AttributeFilterTrait]]

    val DefaultTargetType: String = ObjectTypes.TargetTypeOrigin

    def toQueryOptions(): MultiQueryOptions = {
        val queryPageOptions = pageOptions.toOnlyPageOptions()

        MultiQueryOptions(
            targetType = getObjectTypes().contains(targetType) match {
                case true => targetType
                case false => DefaultTargetType
            },
            objectType = objectType.isEmpty() match {
                case true => None
                case false => Some(objectType)
            },
            query = getAttributeFilters(),
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
