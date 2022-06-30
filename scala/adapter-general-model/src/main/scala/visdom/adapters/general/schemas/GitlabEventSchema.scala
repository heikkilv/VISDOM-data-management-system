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
import visdom.utils.WartRemoverConstants
import visdom.utils.TupleUtils


final case class GitlabEventSchema(
    id: Int,
    project_id: Int,
    author_id: Int,
    action_name: String,
    target_id: Option[Int],
    target_type: Option[String],
    target_title: Option[String],
    created_at: String,
    push_data: Option[GitlabEventPushDataSchema],
    host_name: String
)
extends BaseSchema

object GitlabEventSchema extends BaseSchemaTrait2[GitlabEventSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.ProjectId, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.AuthorId, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.ActionName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.TargetId, true, toIntOption),
        FieldDataModel(SnakeCaseConstants.TargetType, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.TargetTitle, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.CreatedAt, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.PushData, true, GitlabEventPushDataSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[GitlabEventSchema] = {
        TupleUtils.toTuple[Int, Int, Int, String, Option[Int], Option[String], Option[String],
                           String, Option[GitlabEventPushDataSchema], String](values) match {
            case Some(inputValues) => Some(
                (GitlabEventSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
