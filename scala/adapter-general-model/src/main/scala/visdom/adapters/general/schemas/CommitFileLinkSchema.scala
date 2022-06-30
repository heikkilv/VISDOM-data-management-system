// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class CommitFileLinkSchema(
    old_path: String,
    new_path: String,
    a_mode: String,
    b_mode: String,
    new_file: Boolean,
    renamed_file: Boolean,
    deleted_file: Boolean
)
extends BaseSchema

object CommitFileLinkSchema extends BaseSchemaTrait2[CommitFileLinkSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.OldPath, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.NewPath, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.AMode, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.BMode, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.NewFile, false, toBooleanOption),
        FieldDataModel(SnakeCaseConstants.RenamedFile, false, toBooleanOption),
        FieldDataModel(SnakeCaseConstants.DeletedFile, false, toBooleanOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CommitFileLinkSchema] = {
        TupleUtils.toTuple[String, String, String, String, Boolean, Boolean, Boolean](values) match {
            case Some(inputValues) => Some(
                (CommitFileLinkSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
