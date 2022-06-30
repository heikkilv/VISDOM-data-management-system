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


trait GitlabProjectInformationSchemaTrait {
    val project_name: String
    val group_name: String
    val host_name: String
}

final case class GitlabProjectInformationSchema(
    project_name: String,
    group_name: String,
    host_name: String
)
extends BaseSchema
with GitlabProjectInformationSchemaTrait

object GitlabProjectInformationSchema extends BaseSchemaTrait2[GitlabProjectInformationSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.ProjectName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.GroupName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[GitlabProjectInformationSchema] = {
        TupleUtils.toTuple[String, String, String](values) match {
            case Some(inputValues) => Some(
                (GitlabProjectInformationSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
