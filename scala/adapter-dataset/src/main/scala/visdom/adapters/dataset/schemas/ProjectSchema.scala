// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class ProjectSchema(
    project_id: String,
    project_key: String,
    git_link: String,
    jira_link: String,
    sonar_project_key: String
)
extends BaseSchema

object ProjectSchema extends BaseSchemaTrait2[ProjectSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.ProjectId, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.ProjectKey, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.GitLink, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.JiraLink, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.SonarProjectKey, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[ProjectSchema] = {
        TupleUtils.toTuple[String, String, String, String, String](values) match {
            case Some(inputValues) => Some(
                (ProjectSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
