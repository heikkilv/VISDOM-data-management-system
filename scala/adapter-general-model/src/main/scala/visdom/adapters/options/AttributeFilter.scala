package visdom.adapters.options

import org.bson.conversions.Bson
import visdom.utils.GeneralUtils


final case class AttributeFilter(
    attributeName: String,
    filterType: AttributeFilterType,
    targetValue: String
) {
    def getFilter(objectTypes: Seq[String]): Bson = {
        // use the first given object type to determine the attribute types
        objectTypes.headOption match {
            case Some(objectType: String) => ObjectTypes.getAttributeType(objectType, attributeName) match {
                case ObjectTypes.StringType => getFilterString()
                case ObjectTypes.IntType => getFilterInt()
                case ObjectTypes.DoubleType => getFilterDouble()
                case ObjectTypes.BooleanType => getFilterBoolean()
                case _ => getFilterString()
            }
            case None => getFilterString()
        }
    }

    def getFilterString(): Bson = {
        filterType.getFilter(attributeName, targetValue)
    }

    def getFilterInt(): Bson = {
        GeneralUtils.toInt(targetValue) match {
            case Some(target: Int) => filterType.getFilter(attributeName, target)
            case None => AttributeFilterType.emptyFilter
        }
    }

    def getFilterDouble(): Bson = {
        GeneralUtils.toDouble(targetValue) match {
            case Some(target: Double) => filterType.getFilter(attributeName, target)
            case None => AttributeFilterType.emptyFilter
        }
    }

    def getFilterBoolean(): Bson = {
        GeneralUtils.toBooleanOption(targetValue) match {
            case Some(target: Boolean) => filterType.getFilter(attributeName, target)
            case None => AttributeFilterType.emptyFilter
        }
    }
}

object AttributeFilter {
    def fromString(filterString: String): Option[AttributeFilter] = {
        AttributeFilterType.getFilterType(filterString) match {
            case Some(filterType: AttributeFilterType) => {
                val stringParts: Seq[String] = filterString.split(filterType.operatorString)
                stringParts.size == 2 && stringParts.forall(stringPart => stringPart.size > 0) match {
                    case true => Some(
                        AttributeFilter(
                            attributeName = stringParts(0),
                            filterType = filterType,
                            targetValue = stringParts(1)
                        )
                    )
                    case false => None
                }
            }
            case None => None
        }
    }
}
