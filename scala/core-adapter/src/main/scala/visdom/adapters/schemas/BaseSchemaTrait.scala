package visdom.adapters.schemas

import org.apache.spark.sql.Row
import visdom.spark.DataUtils
import visdom.spark.FieldDataType


trait BaseSchemaTrait[T <: BaseSchema] {
    def fields: Seq[FieldDataType]

    def transformValues(valueOptions: Seq[Option[Any]]): Option[T]

    def fromRow(row: Row): Option[T] = {
        DataUtils.getValues(fields, row) match {
            case Some(valueOptions: Seq[Option[Any]]) => {
                transformValues(valueOptions)
            }
            case None => None
        }
    }

    def fromAny(value: Any): Option[T] = {
        value match {
            case Some(someValue) => fromAny(someValue)
            case row: Row => fromRow(row) match {
                case Some(resultValue) => Some(resultValue)
                case None => None
            }
            case _ => None
        }
    }
}
