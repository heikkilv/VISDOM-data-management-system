package visdom.adapters.general.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonNull
import org.mongodb.scala.bson.BsonValue
import spray.json.JsNull
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.schemas.CommitLinksSchema
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.general.schemas.CommitStatsSchema
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class CommitData(
    commit_id: String,
    short_id: String,
    parent_ids: Seq[String],
    committer_name: String,
    committer_email: String,
    author_name: String,
    author_email: String,
    authored_date: String,
    stats: Option[CommitStats],
    title: String,
    web_url: String,
    refs: Seq[CommitRef],
    files: Seq[String]
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.CommitId -> JsonUtils.toBsonValue(commit_id),
                SnakeCaseConstants.ShortId -> JsonUtils.toBsonValue(short_id),
                SnakeCaseConstants.ParentIds -> JsonUtils.toBsonValue(parent_ids),
                SnakeCaseConstants.CommitterName -> JsonUtils.toBsonValue(committer_name),
                SnakeCaseConstants.CommitterEmail -> JsonUtils.toBsonValue(committer_email),
                SnakeCaseConstants.AuthorName -> JsonUtils.toBsonValue(author_name),
                SnakeCaseConstants.AuthorEmail -> JsonUtils.toBsonValue(author_email),
                SnakeCaseConstants.AuthoredDate -> JsonUtils.toBsonValue(authored_date),
                SnakeCaseConstants.Stats -> stats.map(statValues => statValues.toBsonValue()).getOrElse(BsonNull()),
                SnakeCaseConstants.Title -> JsonUtils.toBsonValue(title),
                SnakeCaseConstants.WebUrl -> JsonUtils.toBsonValue(web_url),
                SnakeCaseConstants.Refs -> JsonUtils.toBsonValue(refs.map(ref => ref.toBsonValue())),
                SnakeCaseConstants.Files -> JsonUtils.toBsonValue(files)
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.CommitId -> JsonUtils.toJsonValue(commit_id),
                SnakeCaseConstants.ShortId -> JsonUtils.toJsonValue(short_id),
                SnakeCaseConstants.ParentIds -> JsonUtils.toJsonValue(parent_ids),
                SnakeCaseConstants.CommitterName -> JsonUtils.toJsonValue(committer_name),
                SnakeCaseConstants.CommitterEmail -> JsonUtils.toJsonValue(committer_email),
                SnakeCaseConstants.AuthorName -> JsonUtils.toJsonValue(author_name),
                SnakeCaseConstants.AuthorEmail -> JsonUtils.toJsonValue(author_email),
                SnakeCaseConstants.AuthoredDate -> JsonUtils.toJsonValue(authored_date),
                SnakeCaseConstants.Stats -> stats.map(statValues => statValues.toJsValue()).getOrElse(JsNull),
                SnakeCaseConstants.Title -> JsonUtils.toJsonValue(title),
                SnakeCaseConstants.WebUrl -> JsonUtils.toJsonValue(web_url),
                SnakeCaseConstants.Refs -> JsonUtils.toJsonValue(refs.map(ref => ref.toJsValue())),
                SnakeCaseConstants.Files -> JsonUtils.toJsonValue(files)
            )
        )
    }
}

object CommitData {
    def fromCommitSchema(commitSchema: CommitSchema): CommitData = {
        CommitData(
            commit_id = commitSchema.id,
            short_id = commitSchema.short_id,
            parent_ids = commitSchema.parent_ids,
            committer_name = commitSchema.committer_name,
            committer_email = commitSchema.committer_email,
            author_name = commitSchema.author_name,
            author_email = commitSchema.author_email,
            authored_date =  commitSchema.authored_date,
            stats = commitSchema.stats.map(stats => CommitStats.fromCommitStatsSchema(stats)),
            title = commitSchema.title,
            web_url = commitSchema.web_url,
            refs = commitSchema._links match {
                case Some(commitLinks: CommitLinksSchema) =>
                    commitLinks.refs.map(commitRef => CommitRef.fromCommitRefLinksSchema(commitRef))
                case None => Seq.empty
            },
            // file information simplified to just the file path
            files = commitSchema._links match {
                case Some(commitLinks: CommitLinksSchema) => commitLinks.files.map(file => file.new_path)
                case None => Seq.empty
            }
        )
    }
}
