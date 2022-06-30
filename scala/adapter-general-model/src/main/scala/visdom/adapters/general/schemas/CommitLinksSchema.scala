// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class CommitLinksSchema(
    files: Seq[CommitFileLinkSchema],
    refs: Seq[CommitRefLinkSchema]
)
extends BaseSchema

object CommitLinksSchema extends BaseSchemaTrait2[CommitLinksSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(
            SnakeCaseConstants.Files,
            false,
            (value: Any) => toSeqOption(value, CommitFileLinkSchema.fromAny)
        ),
        FieldDataModel(
            SnakeCaseConstants.Refs,
            false,
            (value: Any) => toSeqOption(value, CommitRefLinkSchema.fromAny)
        )
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CommitLinksSchema] = {
        TupleUtils.toTuple[Seq[CommitFileLinkSchema], Seq[CommitRefLinkSchema]](values) match {
            case Some(inputValues) => Some(
                (CommitLinksSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
