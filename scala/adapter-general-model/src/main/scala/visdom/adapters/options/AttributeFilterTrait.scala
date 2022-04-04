package visdom.adapters.options

import org.bson.conversions.Bson
import visdom.utils.GeneralUtils


trait AttributeFilterTrait {
    val attributeName: String
    val filterType: AttributeFilterType
    val targetValue: String
    val objectTypesObject: ObjectTypesTrait

    def getFilter(objectTypes: Seq[String]): Bson = {
        // use the first given object type to determine the attribute types
        objectTypes.headOption match {
            case Some(objectType: String) => objectTypesObject.getAttributeType(objectType, attributeName) match {
                case objectTypesObject.StringType => getFilterString()
                case objectTypesObject.IntType => getFilterInt()
                case objectTypesObject.DoubleType => getFilterDouble()
                case objectTypesObject.BooleanType => getFilterBoolean()
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
