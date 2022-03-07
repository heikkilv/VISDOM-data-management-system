package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.GeneralUtils.toStringSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants
import visdom.adapters.general.model.events.CommitEvent


final case class GitlabAuthorSchema(
    committer_name: String,
    committer_email: String,
    host_name: String,
    related_commit_event_ids: Seq[String]
)
extends BaseSchema

object GitlabAuthorSchema extends BaseSchemaTrait2[GitlabAuthorSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.CommitterName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitterEmail, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.RelatedCommitEventIds, false, toStringSeqOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[GitlabAuthorSchema] = {
        TupleUtils.toTuple[String, String, String, Seq[String]](values) match {
            case Some(inputValues) => Some(
                (GitlabAuthorSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }

    def fromCommitAuthorSimpleSchema(commitAuthorSimpleSchema: CommitAuthorSimpleSchema): GitlabAuthorSchema = {
        val commitEventId: String = CommitEvent.getId(
            hostName = commitAuthorSimpleSchema.host_name,
            projectName = commitAuthorSimpleSchema.project_name,
            commitId = commitAuthorSimpleSchema.id
        )
        GitlabAuthorSchema(
            committer_name = commitAuthorSimpleSchema.committer_name,
            committer_email = commitAuthorSimpleSchema.committer_email,
            host_name = commitAuthorSimpleSchema.host_name,
            related_commit_event_ids = Seq(commitEventId)
        )
    }

    def reduceSchemas(
        firstAuthorSchema: GitlabAuthorSchema,
        secondAuthorSchema: GitlabAuthorSchema
    ): GitlabAuthorSchema = {
        // NOTE: assume that both schemas have the same name, email, and host
        GitlabAuthorSchema(
            committer_name = firstAuthorSchema.committer_name,
            committer_email = firstAuthorSchema.committer_email,
            host_name = firstAuthorSchema.host_name,
            related_commit_event_ids =
                firstAuthorSchema.related_commit_event_ids ++ secondAuthorSchema.related_commit_event_ids
        )
    }

    // def fromCommitAuthorSimpleSchemas(
    //     commitAuthorSimpleSchemas: Seq[CommitAuthorSimpleSchema]
    // ): Option[GitlabAuthorSchema] = {
    //     commitAuthorSimpleSchemas.headOption match {
    //         case Some(firstSchema: CommitAuthorSimpleSchema) =>
    //             commitAuthorSimpleSchemas.find(
    //                 schema =>
    //                     schema.committer_email != firstSchema.committer_email ||
    //                     schema.host_name != firstSchema.host_name
    //             ) match {
    //                 case Some(_) => None  // the sequence contains commits from multiple emails or from multiple hosts
    //                 case None => Some(
    //                     GitlabAuthorSchema(
    //                         committer_name = firstSchema.committer_name,
    //                         committer_email = firstSchema.committer_email,
    //                         host_name = firstSchema.host_name,
    //                         related_commit_event_ids = commitAuthorSimpleSchemas.map(
    //                             schema => CommitEvent.getId(
    //                                 hostName = schema.host_name,
    //                                 projectName = schema.project_name,
    //                                 commitId = schema.id
    //                             )
    //                         )
    //                     )
    //                 )
    //             }
    //         case None => None  // the sequence is empty
    //     }
    // }
}
