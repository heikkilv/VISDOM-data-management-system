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


final case class PipelineUserSchema(
    id: Int,
    state: String,
    name: String,
    username: String,
    avatar_url: String,
    web_url: String
)
extends BaseSchema

object PipelineUserSchema extends BaseSchemaTrait2[PipelineUserSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.State, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Name, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Username, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.AvatarUrl, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.WebUrl, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PipelineUserSchema] = {
        TupleUtils.toTuple[Int, String, String, String, String, String](values) match {
            case Some(inputValues) => Some(
                (PipelineUserSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
