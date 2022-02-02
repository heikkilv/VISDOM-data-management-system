package visdom.spark

import org.apache.spark.sql.Row
import visdom.utils.WartRemoverConstants


object DataUtils {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def getValues(fields: Seq[FieldDataType], row: Row): Option[Seq[Option[Any]]] = {
        val rowFields: Seq[String] = row.schema.fields.map(rowField => rowField.name)

        def getFieldValue(field: FieldDataType, index: Int): Option[Option[Any]] = {
            row.isNullAt(index) match {
                case true => field.nullable match {
                    case true => Some(None)
                    case false => None
                }
                case false => Some(Some(row.get(index)))
            }
        }

        def getValuesInternal(fieldSeq: Seq[FieldDataType], values: Seq[Option[Any]]): Option[Seq[Option[Any]]] = {
            fieldSeq.headOption match {
                case Some(field: FieldDataType) => rowFields.contains(field.name) match {
                    case true => {
                        val index: Int = row.schema.fieldIndex(field.name)
                        getFieldValue(field, index) match {
                            case Some(fieldValueOption: Option[Any]) =>
                                getValuesInternal(fieldSeq.drop(1), values ++ Seq(fieldValueOption))
                            case None => None  // value that is not nullable was not found
                        }
                    }
                    case false => field.nullable match {
                        case true => getValuesInternal(fieldSeq.drop(1), values ++ Seq(None))
                        case false => None  // value that is not nullable was not found
                    }
                }
                case None => Some(values)
            }
        }

        getValuesInternal(fields, Seq.empty)
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def getTransformedValues(fields: Seq[FieldDataModel], row: Row): Option[Seq[Any]] = {
        val rowFields: Seq[String] = row.schema.fields.map(rowField => rowField.name)

        def getFieldValue(field: FieldDataModel, index: Int): Option[Any] = {
            (row.isNullAt(index) match {
                case true => None
                case false => field.transformation(row.get(index))
            }) match {
                case Some(fieldValue: Any) => field.nullable match {
                     case true => Some(Some(fieldValue))  // a proper value for nullable field
                     case false => Some(fieldValue)       // a proper value for non-nullable field
                }
                case None => field.nullable match {
                    case true => Some(None)  // no valid value for nullable field
                    case false => None       // no valid value for non-nullable field
                }
            }
        }

        def getValuesInternal(fieldSeq: Seq[FieldDataModel], values: Seq[Any]): Option[Seq[Any]] = {
            fieldSeq.headOption match {
                case Some(field: FieldDataModel) => rowFields.contains(field.name) match {
                    case true => {
                        val index: Int = row.schema.fieldIndex(field.name)
                        getFieldValue(field, index) match {
                            case Some(fieldValueOption: Any) =>
                                getValuesInternal(fieldSeq.drop(1), values ++ Seq(fieldValueOption))
                            case None => None  // value that is not nullable was not found
                                               // or the value could not be transformed properly
                        }
                    }
                    case false => field.nullable match {
                        case true => getValuesInternal(fieldSeq.drop(1), values ++ Seq(None))
                        case false => None  // value that is not nullable was not found
                    }
                }
                case None => Some(values)
            }
        }

        getValuesInternal(fields, Seq.empty)
    }
}
