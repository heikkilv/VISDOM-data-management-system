package visdom.adapters.general.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.schemas.CommitSchema
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
    stats: CommitStats,
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
                SnakeCaseConstants.Stats -> stats.toBsonValue(),
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
                SnakeCaseConstants.Stats -> stats.toJsValue(),
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
            stats = CommitStats(
                additions = commitSchema.stats.additions,
                deletions = commitSchema.stats.deletions,
                total = commitSchema.stats.total
            ),
            title = commitSchema.title,
            webUrl = commitSchema.web_url,
            refs = commitSchema._links.refs.map(
                commitRef => CommitRef(refType = commitRef.`type`, name = commitRef.name)
            ),
            // file information simplified to just the file path
            files = commitSchema._links.files.map(
                file => file.new_path
            )
        )
    }
}
