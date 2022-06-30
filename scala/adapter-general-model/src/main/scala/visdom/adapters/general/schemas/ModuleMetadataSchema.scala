// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class ModuleMetadataSchema(
    other: Option[ModuleMetadataOtherSchema]
)
extends BaseSchema

object ModuleMetadataSchema extends BaseSchemaTrait2[ModuleMetadataSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Other, true, ModuleMetadataOtherSchema.fromAny)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[ModuleMetadataSchema] = {
        TupleUtils.toTuple[Option[ModuleMetadataOtherSchema]](values) match {
            case Some(inputValues) => Some(ModuleMetadataSchema(inputValues._1))
            case None => None
        }
    }
}
