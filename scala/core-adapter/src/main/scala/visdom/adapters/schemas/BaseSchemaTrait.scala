// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.schemas

import org.apache.spark.sql.Row
import visdom.spark.DataUtils
import visdom.spark.FieldDataModel
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

trait BaseSchemaTrait2[T <: BaseSchema] {
    def fields: Seq[FieldDataModel]

    def createInstance(values: Seq[Any]): Option[T]

    // Example code for createInstance
    //
    // @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    // def createInstance(values: Seq[Any]): Option[T] = {
    //     TupleUtils.toTuple[A, B, C](values) match {
    //         case Some(inputValues) => Some(
    //             (T.apply _).tupled(inputValues)
    //         )
    //         case None => None
    //     }
    // }

    def fromRow(row: Row): Option[T] = {
        DataUtils.getTransformedValues(fields, row) match {
            case Some(values: Seq[Any]) => {
                createInstance(values)
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
