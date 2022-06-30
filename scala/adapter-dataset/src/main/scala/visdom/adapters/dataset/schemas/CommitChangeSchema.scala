// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class CommitChangeSchema(
    project_id: String,
    file: String,
    commit_hash: String,
    date: String,
    committer_id: String,
    lines_added: Int,
    lines_removed: Int,
    note: String
)
extends BaseSchema

object CommitChangeSchema extends BaseSchemaTrait2[CommitChangeSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.ProjectId, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.File, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitHash, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Date, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitterId, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.LinesAdded, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.LinesRemoved, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Note, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CommitChangeSchema] = {
        TupleUtils.toTuple[String, String, String, String, String, Int, Int, String](values) match {
            case Some(inputValues) => Some(
                (CommitChangeSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
