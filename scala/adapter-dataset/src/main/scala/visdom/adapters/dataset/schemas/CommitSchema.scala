package visdom.adapters.dataset.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.GeneralUtils.toStringSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class CommitSchema(
    project_id: String,
    commit_hash: String,
    commit_message: String,
    committer: String,
    committer_date: String,
    parents: Seq[String],
    author: String,
    author_date: String,
    branches: Seq[String],
    merge: Boolean
)
extends BaseSchema

object CommitSchema extends BaseSchemaTrait2[CommitSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.ProjectId, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitHash, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitMessage, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Committer, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitterDate, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Parents, false, toStringSeqOption),
        FieldDataModel(SnakeCaseConstants.Author, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.AuthorDate, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Branches, false, toStringSeqOption),
        FieldDataModel(SnakeCaseConstants.Merge, false, toBooleanOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CommitSchema] = {
        TupleUtils.toTuple[String, String, String, String, String, Seq[String],
                           String, String, Seq[String], Boolean](values) match {
            case Some(inputValues) => Some(
                (CommitSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
