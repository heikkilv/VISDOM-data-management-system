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


final case class CommitAuthorSimpleSchema(
    id: String,
    project_name: String,
    host_name: String,
    committer_name: String,
    committer_email: String
)
extends BaseSchema

object CommitAuthorSimpleSchema extends BaseSchemaTrait2[CommitAuthorSimpleSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.ProjectName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitterName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitterEmail, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CommitAuthorSimpleSchema] = {
        TupleUtils.toTuple[String, String, String, String, String](values) match {
            case Some(inputValues) => Some(
                (CommitAuthorSimpleSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
