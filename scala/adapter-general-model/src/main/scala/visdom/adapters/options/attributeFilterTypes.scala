package visdom.adapters.options

import org.bson.conversions.Bson
import org.mongodb.scala.model.Filters
import visdom.utils.CommonConstants
import visdom.utils.SnakeCaseConstants


abstract class AttributeFilterType {
    val operatorString: String
    def getFilter[TItem](attributeName: String, value: TItem): Bson
}

final case object AttributeFilterEqual
extends AttributeFilterType {
    val operatorString: String = CommonConstants.DoubleEqual

    def getFilter[TItem](attributeName: String, value: TItem): Bson = {
        Filters.equal(attributeName, value)
    }
}

final case object AttributeFilterNotEqual
extends AttributeFilterType {
    val operatorString: String = CommonConstants.NotEqual

    def getFilter[TItem](attributeName: String, value: TItem): Bson = {
        Filters.ne(attributeName, value)
    }
}

final case object AttributeFilterLessThan
extends AttributeFilterType {
    val operatorString: String = CommonConstants.LessThan

    def getFilter[TItem](attributeName: String, value: TItem): Bson = {
        Filters.lt(attributeName, value)
    }
}

final case object AttributeFilterLessThanOrEqual
extends AttributeFilterType {
    val operatorString: String = CommonConstants.LessThanOrEqual

    def getFilter[TItem](attributeName: String, value: TItem): Bson = {
        Filters.lte(attributeName, value)
    }
}

final case object AttributeFilterGreaterThan
extends AttributeFilterType {
    val operatorString: String = CommonConstants.GreaterThan

    def getFilter[TItem](attributeName: String, value: TItem): Bson = {
        Filters.gt(attributeName, value)
    }
}

final case object AttributeFilterGreaterThanOrEqual
extends AttributeFilterType {
    val operatorString: String = CommonConstants.GreaterThanOrEqual

    def getFilter[TItem](attributeName: String, value: TItem): Bson = {
        Filters.gte(attributeName, value)
    }
}

final case object AttributeFilterRegex
extends AttributeFilterType {
    val operatorString: String = CommonConstants.Regex

    def getFilter[TItem](attributeName: String, value: TItem): Bson = {
        value match {
            case pattern: String => Filters.regex(attributeName, pattern)
            case _ => AttributeFilterType.emptyFilter
        }
    }
}

object AttributeFilterType {
    val filterTypes: Array[AttributeFilterType] = Array(
        AttributeFilterEqual,
        AttributeFilterNotEqual,
        AttributeFilterLessThanOrEqual,
        AttributeFilterGreaterThanOrEqual,
        AttributeFilterRegex,
        AttributeFilterLessThan,
        AttributeFilterGreaterThan
    )

    val emptyFilter: Bson = Filters.exists(SnakeCaseConstants.Id)

    def getFilterType(filterString: String): Option[AttributeFilterType] = {
        def getFilterTypeInternal(filterTypesRemaining: Seq[AttributeFilterType]): Option[AttributeFilterType] = {
            filterTypesRemaining.headOption match {
                case Some(filterType: AttributeFilterType) => filterString.contains(filterType.operatorString) match {
                    case true => Some(filterType)
                    case false => getFilterTypeInternal(filterTypesRemaining.drop(1))
                }
                case None => None
            }
        }

        getFilterTypeInternal(filterTypes)
    }
}
