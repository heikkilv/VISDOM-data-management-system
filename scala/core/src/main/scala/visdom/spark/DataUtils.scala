package visdom.spark

import org.apache.spark.sql.Row
import visdom.utils.WartRemoverConstants


object DataUtils {

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def getValues(fields: Seq[FieldDataType], row: Row): Option[Seq[Option[Any]]] = {

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
            val rowFields: Seq[String] = row.schema.fields.map(rowField => rowField.name)
            fieldSeq.headOption match {
                case Some(field: FieldDataType) => rowFields.contains(field.name) match {
                    case true => {
                        val index: Int = row.schema.fieldIndex(field.name)
                        getFieldValue(field, index) match {
                            case Some(fieldValueOption: Option[Any]) =>
                                getValuesInternal(fieldSeq.drop(1), values ++ Seq(fieldValueOption))
                            case None => None
                        }
                    }
                    case false => None
                }
                case None => Some(values)
            }
        }

        getValuesInternal(fields, Seq.empty)
    }
}
