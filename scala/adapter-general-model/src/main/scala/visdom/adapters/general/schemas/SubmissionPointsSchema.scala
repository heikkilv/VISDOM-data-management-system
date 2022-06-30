// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class SubmissionPointsSchema(
    id: Int,
    submission_time: String,
    grade: Int,
    url: String
)
extends BaseSchema

object SubmissionPointsSchema extends BaseSchemaTrait2[SubmissionPointsSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.SubmissionTime, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Grade, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Url, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[SubmissionPointsSchema] = {
        TupleUtils.toTuple[Int, String, Int, String](values) match {
            case Some(inputValues) => Some(
                (SubmissionPointsSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
