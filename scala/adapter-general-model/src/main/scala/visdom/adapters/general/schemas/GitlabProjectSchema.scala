package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


trait GitlabProjectSchemaTrait {
    val project_name: String
    val group_name: String
    val host_name: String
}

final case class GitlabProjectSchema(
    project_name: String,
    group_name: String,
    host_name: String
)
extends BaseSchema
with GitlabProjectSchemaTrait

object GitlabProjectSchema extends BaseSchemaTrait2[GitlabProjectSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.ProjectName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.GroupName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[GitlabProjectSchema] = {
        TupleUtils.toTuple[String, String, String](values) match {
            case Some(inputValues) => Some(
                (GitlabProjectSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
