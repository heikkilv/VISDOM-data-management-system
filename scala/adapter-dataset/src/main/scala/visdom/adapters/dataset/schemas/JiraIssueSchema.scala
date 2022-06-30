// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toDoubleOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class JiraIssueSchema(
    project_id: String,
    key: String,
    priority: String,
    `type`: String,
    status: String,
    resolution: Option[String],
    reporter: String,
    creator_name: String,
    assignee: Option[String],
    summary: String,
    description: String,
    time_original_estimate: Option[Double],
    time_spent: Option[Double],
    time_estimate: Option[Double],
    creation_date: String,
    resolution_date: Option[String],
    update_date: String,
    due_date: Option[String],
    progress_percent: Option[Double],
    hash: Option[String],
    commit_date: Option[String]
)
extends BaseSchema

object JiraIssueSchema extends BaseSchemaTrait2[JiraIssueSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.ProjectId, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Key, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Priority, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Type, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Status, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Resolution, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Reporter, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CreatorName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Assignee, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Summary, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Description, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.TimeOriginalEstimate, true, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.TimeSpent, true, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.TimeEstimate, true, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.CreationDate, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.ResolutionDate, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.UpdateDate, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.DueDate, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.ProgressPercent, true, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.Hash, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitDate, true, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[JiraIssueSchema] = {
        TupleUtils.toTuple[String, String, String, String, String, Option[String], String, String, Option[String],
                           String, String, Option[Double], Option[Double], Option[Double], String, Option[String],
                           String, Option[String], Option[Double], Option[String], Option[String]](values) match {
            case Some(inputValues) => Some(
                (JiraIssueSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
