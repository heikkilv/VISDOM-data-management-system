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


final case class ModuleNameSchema(
    number: Option[String],
    fi: Option[String],
    en: Option[String],
    raw: String
)
extends BaseSchema

object ModuleNameSchema extends BaseSchemaTrait2[ModuleNameSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Number, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Fi, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.En, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Raw, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[ModuleNameSchema] = {
        TupleUtils.toTuple[Option[String], Option[String], Option[String], String](values) match {
            case Some(inputValues) => Some(
                (ModuleNameSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
