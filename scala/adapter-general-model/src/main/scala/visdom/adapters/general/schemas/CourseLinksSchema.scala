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


final case class CourseLinksSchema(
    modules: Option[Seq[Int]],
    points: Option[Seq[Int]]
)
extends BaseSchema

object CourseLinksSchema extends BaseSchemaTrait2[CourseLinksSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Modules, true, (value: Any) => toSeqOption(value, toIntOption)),
        FieldDataModel(SnakeCaseConstants.Points, true, (value: Any) => toSeqOption(value, toIntOption))
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CourseLinksSchema] = {
        TupleUtils.toTuple[Option[Seq[Int]], Option[Seq[Int]]](values) match {
            case Some(inputValues) => Some(
                (CourseLinksSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
