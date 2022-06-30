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


final case class CommitRefLinkSchema(
    `type`: String,
    name: String
)
extends BaseSchema

object CommitRefLinkSchema extends BaseSchemaTrait2[CommitRefLinkSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Type, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Name, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CommitRefLinkSchema] = {
        TupleUtils.toTuple[String, String](values) match {
            case Some(inputValues) => Some(
                (CommitRefLinkSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
