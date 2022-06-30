// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class CourseNameSchema(
    fi: Option[NameSchema],
    en: Option[NameSchema],
    raw: String
)
extends BaseSchema

object CourseNameSchema extends BaseSchemaTrait2[CourseNameSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Fi, true, NameSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.En, true, NameSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Raw, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CourseNameSchema] = {
        TupleUtils.toTuple[Option[NameSchema], Option[NameSchema], String](values) match {
            case Some(inputValues) => Some(
                (CourseNameSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
