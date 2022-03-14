package visdom.http.server.options

import visdom.adapters.options.AttributeFilter
import visdom.adapters.options.MultiQueryOptions
import visdom.adapters.options.ObjectTypes
import visdom.http.server.services.constants.GeneralAdapterConstants
import visdom.utils.CommonConstants


final case class MultiOptions(
    pageOptions: OnlyPageInputOptions,
    targetType: String,
    objectType: String,
    query: Option[String],
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
            query = MultiOptions.getAttributeFilters(query),
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

object MultiOptions {
    def getAttributeFilters(queryStringOption: Option[String]): Option[Seq[AttributeFilter]] = {
        (
            queryStringOption.map(
                queryString =>
                    queryString
                        .split(CommonConstants.Semicolon)
                        .map(attributeQuery => AttributeFilter.fromString(attributeQuery))
                        .flatten
                        .toSeq
            )
        ) match {
            case Some(filters: Seq[AttributeFilter]) => filters.nonEmpty match {
                case true => Some(filters)
                case false => None
            }
            case None => None
        }
    }
}
