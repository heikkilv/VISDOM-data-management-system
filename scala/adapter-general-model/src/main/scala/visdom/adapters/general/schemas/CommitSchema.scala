package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.GeneralUtils.toStringSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
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
    stats: Option[CommitStatsSchema],
    _links: Option[CommitLinksSchema]
)
extends BaseSchema

object CommitSchema extends BaseSchemaTrait2[CommitSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Id, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.ShortId, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.ParentIds, false, toStringSeqOption),
        FieldDataModel(SnakeCaseConstants.ProjectName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.GroupName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Message, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Title, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommittedDate, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitterName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.CommitterEmail, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.AuthoredDate, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.AuthorName, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.AuthorEmail, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.WebUrl, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Stats, true, CommitStatsSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Links, true, CommitLinksSchema.fromAny)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[CommitSchema] = {
        TupleUtils.toTuple[String, String, Seq[String], String, String, String,
                           String, String, String, String, String, String, String,
                           String, String, Option[CommitStatsSchema], Option[CommitLinksSchema]](values) match {
            case Some(inputValues) => Some(
                (CommitSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
