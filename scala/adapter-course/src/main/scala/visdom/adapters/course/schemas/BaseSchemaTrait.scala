// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.course.schemas

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
