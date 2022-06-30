// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.TupleUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class CommitStatsSchema(
    additions: Int,
    deletions: Int,
    total: Int
)
extends BaseSchema

object CommitStatsSchema extends BaseSchemaTrait2[CommitStatsSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Additions, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Deletions, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Total, false, toIntOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CommitStatsSchema] = {
        TupleUtils.toTuple[Int, Int, Int](values) match {
            case Some(inputValues) => Some(
                (CommitStatsSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
