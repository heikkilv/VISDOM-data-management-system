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


final case class GitlabProjectSchema(
    id: Int,
    path_with_namespace: String,
    namespace: GitlabNamespaceSchema,
    host_name: String
)
extends BaseSchema

object GitlabProjectSchema extends BaseSchemaTrait2[GitlabProjectSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.PathWithNamespace, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Namespace, false, GitlabNamespaceSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[GitlabProjectSchema] = {
        TupleUtils.toTuple[Int, String, GitlabNamespaceSchema, String](values) match {
            case Some(inputValues) => Some(
                (GitlabProjectSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
