package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class FileSchema(
    id: String,
    `type`: String,
    name: String,
    path: String,
    mode: String,
    project_name: String,
    group_name: String,
    host_name: String,
    _links: Option[FileLinksSchema]
)
extends BaseSchema

object FileSchema extends BaseSchemaTrait2[FileSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Type, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Name, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Path, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Mode, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.ProjectName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.GroupName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Links, true, FileLinksSchema.fromAny)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[FileSchema] = {
        TupleUtils.toTuple[String, String, String, String, String, String,
                           String, String, Option[FileLinksSchema]](values) match {
            case Some(inputValues) => Some(
                (FileSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
