package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.GeneralUtils.toStringSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class CommitAuthorSchema(
    committer_name: String,
    committer_email: String,
    host_name: String,
    related_commit_event_ids: Seq[String]
)
extends BaseSchema

object CommitAuthorSchema extends BaseSchemaTrait2[CommitAuthorSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.CommitterName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitterEmail, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.RelatedCommitEventIds, false, toStringSeqOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CommitAuthorSchema] = {
        TupleUtils.toTuple[String, String, String, Seq[String]](values) match {
            case Some(inputValues) => Some(
                (CommitAuthorSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }

    def reduceSchemas(
        firstAuthorSchema: CommitAuthorSchema,
        secondAuthorSchema: CommitAuthorSchema
    ): CommitAuthorSchema = {
        // NOTE: assume that both schemas have the same name, email, and host
        CommitAuthorSchema(
            committer_name = firstAuthorSchema.committer_name,
            committer_email = firstAuthorSchema.committer_email,
            host_name = firstAuthorSchema.host_name,
            related_commit_event_ids =
                firstAuthorSchema.related_commit_event_ids ++ secondAuthorSchema.related_commit_event_ids
        )
    }
}
