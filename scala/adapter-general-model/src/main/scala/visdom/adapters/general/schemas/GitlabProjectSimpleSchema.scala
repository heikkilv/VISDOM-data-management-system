package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class GitlabProjectSimpleSchema(
    project_id: Option[Int],
    project_name: String,
    group_name: String,
    host_name: String
)
extends BaseSchema
with GitlabProjectInformationSchemaTrait

object GitlabProjectSimpleSchema {
    def fromProjectInformationSchema(projectInformationSchema: GitlabProjectInformationSchema): GitlabProjectSimpleSchema = {
        GitlabProjectSimpleSchema(
            project_id = None,
            project_name = projectInformationSchema.project_name,
            group_name = projectInformationSchema.group_name,
            host_name = projectInformationSchema.host_name
        )
    }

    def fromProjectSchema(projectSchema: GitlabProjectSchema): GitlabProjectSimpleSchema = {
        GitlabProjectSimpleSchema(
            project_id = Some(projectSchema.id),
            project_name = projectSchema.path_with_namespace,
            group_name = projectSchema.namespace.full_path,
            host_name = projectSchema.host_name
        )
    }
}
