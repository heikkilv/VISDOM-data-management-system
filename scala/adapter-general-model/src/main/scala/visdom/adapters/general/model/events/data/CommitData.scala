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
    commitId: String,
    shortId: String,
    parentIds: Seq[String],
    committerName: String,
    committerEmail: String,
    authorName: String,
    authorEmail: String,
    authoredDate: String,
    stats: Option[CommitStats],
    title: String,
    webUrl: String,
    refs: Seq[CommitRef],
    files: Seq[String]
)
extends Data
with BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.CommitId -> JsonUtils.toBsonValue(commitId),
                SnakeCaseConstants.ShortId -> JsonUtils.toBsonValue(shortId),
                SnakeCaseConstants.ParentIds -> JsonUtils.toBsonValue(parentIds),
                SnakeCaseConstants.CommitterName -> JsonUtils.toBsonValue(committerName),
                SnakeCaseConstants.CommitterEmail -> JsonUtils.toBsonValue(committerEmail),
                SnakeCaseConstants.AuthorName -> JsonUtils.toBsonValue(authorName),
                SnakeCaseConstants.AuthorEmail -> JsonUtils.toBsonValue(authorEmail),
                SnakeCaseConstants.AuthoredDate -> JsonUtils.toBsonValue(authoredDate),
                SnakeCaseConstants.Stats -> stats.map(statValues => statValues.toBsonValue()).getOrElse(BsonNull()),
                SnakeCaseConstants.Title -> JsonUtils.toBsonValue(title),
                SnakeCaseConstants.WebUrl -> JsonUtils.toBsonValue(webUrl),
                SnakeCaseConstants.Refs -> JsonUtils.toBsonValue(refs.map(ref => ref.toBsonValue())),
                SnakeCaseConstants.Files -> JsonUtils.toBsonValue(files)
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.CommitId -> JsonUtils.toJsonValue(commitId),
                SnakeCaseConstants.ShortId -> JsonUtils.toJsonValue(shortId),
                SnakeCaseConstants.ParentIds -> JsonUtils.toJsonValue(parentIds),
                SnakeCaseConstants.CommitterName -> JsonUtils.toJsonValue(committerName),
                SnakeCaseConstants.CommitterEmail -> JsonUtils.toJsonValue(committerEmail),
                SnakeCaseConstants.AuthorName -> JsonUtils.toJsonValue(authorName),
                SnakeCaseConstants.AuthorEmail -> JsonUtils.toJsonValue(authorEmail),
                SnakeCaseConstants.AuthoredDate -> JsonUtils.toJsonValue(authoredDate),
                SnakeCaseConstants.Stats -> stats.map(statValues => statValues.toJsValue()).getOrElse(JsNull),
                SnakeCaseConstants.Title -> JsonUtils.toJsonValue(title),
                SnakeCaseConstants.WebUrl -> JsonUtils.toJsonValue(webUrl),
                SnakeCaseConstants.Refs -> JsonUtils.toJsonValue(refs.map(ref => ref.toJsValue())),
                SnakeCaseConstants.Files -> JsonUtils.toJsonValue(files)
            )
        )
    }
}

object CommitData {
    def fromCommitSchema(commitSchema: CommitSchema): CommitData = {
        CommitData(
            commitId = commitSchema.id,
            shortId = commitSchema.short_id,
            parentIds = commitSchema.parent_ids,
            committerName = commitSchema.committer_name,
            committerEmail = commitSchema.committer_email,
            authorName = commitSchema.author_name,
            authorEmail = commitSchema.author_email,
            authoredDate =  commitSchema.authored_date,
            stats = commitSchema.stats.map(stats => CommitStats.fromCommitStatsSchema(stats)),
            title = commitSchema.title,
            webUrl = commitSchema.web_url,
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
