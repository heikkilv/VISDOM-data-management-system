// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.GeneralUtils.toStringSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class CommitSimpleSchema(
    id: String,
    parent_ids: Seq[String],
    committer_email: String,
    project_name: String,
    host_name: String
)
extends BaseSchema

object CommitSimpleSchema extends BaseSchemaTrait2[CommitSimpleSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.ParentIds, false, toStringSeqOption),
        FieldDataModel(SnakeCaseConstants.CommitterEmail, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.ProjectName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CommitSimpleSchema] = {
        TupleUtils.toTuple[String, Seq[String], String, String, String](values) match {
            case Some(inputValues) => Some(
                (CommitSimpleSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
