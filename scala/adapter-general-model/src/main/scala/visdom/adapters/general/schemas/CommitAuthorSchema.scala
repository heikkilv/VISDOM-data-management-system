package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class CommitAuthorSchema(
    id: String,
    committer_name: String,
    committer_email: String,
    host_name: String,
    project_name: String
)
extends BaseSchema

object CommitAuthorSchema extends BaseSchemaTrait2[CommitAuthorSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitterName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitterEmail, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.ProjectName, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CommitAuthorSchema] = {
        TupleUtils.toTuple[String, String, String, String, String](values) match {
            case Some(inputValues) => Some(
                (CommitAuthorSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
