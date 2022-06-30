// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class ModuleLinksSchema(
    courses: Option[Int],
    exercises: Option[Seq[Int]]
)
extends BaseSchema

object ModuleLinksSchema extends BaseSchemaTrait2[ModuleLinksSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Courses, true, toIntOption),
        FieldDataModel(SnakeCaseConstants.Exercises, true, (value: Any) => toSeqOption(value, toIntOption))
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[ModuleLinksSchema] = {
        TupleUtils.toTuple[Option[Int], Option[Seq[Int]]](values) match {
            case Some(inputValues) => Some(
                (ModuleLinksSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
