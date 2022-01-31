package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait
import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.GeneralUtils.toStringSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.toOption
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class CommitSchema(
    id: String,
    short_id: String,
    parent_ids: Seq[String],
    project_name: String,
    group_name: String,
    host_name: String,
    message: String,
    title: String,
    committed_date: String,
    committer_name: String,
    committer_email: String,
    authored_date: String,
    author_name: String,
    author_email: String,
    web_url: String,
    stats: CommitStatsSchema,
    _links: CommitLinksSchema
)
extends BaseSchema

object CommitSchema extends BaseSchemaTrait[CommitSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.ShortId, false),
        FieldDataType(SnakeCaseConstants.ParentIds, false),
        FieldDataType(SnakeCaseConstants.ProjectName, false),
        FieldDataType(SnakeCaseConstants.GroupName, false),
        FieldDataType(SnakeCaseConstants.HostName, false),
        FieldDataType(SnakeCaseConstants.Message, false),
        FieldDataType(SnakeCaseConstants.Title, false),
        FieldDataType(SnakeCaseConstants.CommittedDate, false),
        FieldDataType(SnakeCaseConstants.CommitterName, false),
        FieldDataType(SnakeCaseConstants.CommitterEmail, false),
        FieldDataType(SnakeCaseConstants.AuthoredDate, false),
        FieldDataType(SnakeCaseConstants.AuthorName, false),
        FieldDataType(SnakeCaseConstants.AuthorEmail, false),
        FieldDataType(SnakeCaseConstants.WebUrl, false),
        FieldDataType(SnakeCaseConstants.Stats, false),
        FieldDataType(SnakeCaseConstants.Links, false)
    )

    // scalastyle:off method.length
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[CommitSchema] = {
        toOption(
            valueOptions.toTuple17,
            (
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringSeqOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => CommitStatsSchema.fromAny(value),
                (value: Any) => CommitLinksSchema.fromAny(value)
            )
        ) match {
            case Some((
                id: String,
                short_id: String,
                parent_ids: Seq[String],
                project_name: String,
                group_name: String,
                host_name: String,
                message: String,
                title: String,
                committed_date: String,
                committer_name: String,
                committer_email: String,
                authored_date: String,
                author_name: String,
                author_email: String,
                web_url: String,
                stats: CommitStatsSchema,
                links: CommitLinksSchema
            )) => Some(
                CommitSchema(
                    id,
                    short_id,
                    parent_ids,
                    project_name,
                    group_name,
                    host_name,
                    message,
                    title,
                    committed_date,
                    committer_name,
                    committer_email,
                    authored_date,
                    author_name,
                    author_email,
                    web_url,
                    stats,
                    links
                )
            )
            case None => None
        }
    }
    // scalastyle:on method.length
}
