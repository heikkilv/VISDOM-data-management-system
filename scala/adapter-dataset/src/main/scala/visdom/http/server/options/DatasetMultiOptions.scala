package visdom.http.server.options

import visdom.adapters.options.AttributeFilterTrait
import visdom.adapters.options.DatasetAttributeFilter
import visdom.adapters.options.DatasetObjectTypes
import visdom.utils.CommonConstants


final case class DatasetMultiOptions(
    pageOptions: OnlyPageInputOptions,
    targetType: String,
    objectType: String,
    query: Option[String],
    dataAttributes: Option[String],
    includedLinks: String
)
extends BaseMultiInputOptions {
    def getObjectTypes(): Map[String, Set[String]] = {
        DatasetObjectTypes.objectTypes
    }

    def getAttributeFilters(): Option[Seq[AttributeFilterTrait]] = {
        DatasetMultiOptions.getAttributeFilters(query)
    }
}

object DatasetMultiOptions {
    def getAttributeFilters(queryStringOption: Option[String]): Option[Seq[DatasetAttributeFilter]] = {
        (
            queryStringOption.map(
                queryString =>
                    queryString
                        .split(CommonConstants.Semicolon)
                        .map(attributeQuery => DatasetAttributeFilter.fromString(attributeQuery))
                        .flatten
                        .toSeq
            )
        ) match {
            case Some(filters: Seq[DatasetAttributeFilter]) => filters.nonEmpty match {
                case true => Some(filters)
                case false => None
            }
            case None => None
        }
    }
}
