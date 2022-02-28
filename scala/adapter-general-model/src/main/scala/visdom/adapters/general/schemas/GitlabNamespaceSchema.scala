package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class GitlabNamespaceSchema(
    id: Int,
    name: String,
    path: String,
    kind: String,
    full_path: String
)
extends BaseSchema

object GitlabNamespaceSchema extends BaseSchemaTrait2[GitlabNamespaceSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.Name, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Path, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Kind, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.FullPath, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[GitlabNamespaceSchema] = {
        TupleUtils.toTuple[Int, String, String, String, String](values) match {
            case Some(inputValues) => Some(
                (GitlabNamespaceSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
