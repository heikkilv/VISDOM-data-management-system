package visdom.adapters.dataset.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.dataset.schemas.CommitSchema
import visdom.adapters.general.model.base.Data
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ProjectCommitData(
    commit_id: String,
    parent_ids: Seq[String],
    committer_name: String,
    author_name: String,
    authored_date: String,
    stats: ProjectCommitStats,
    refs: Seq[String]
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.CommitId -> JsonUtils.toBsonValue(commit_id),
                SnakeCaseConstants.ParentIds -> JsonUtils.toBsonValue(parent_ids),
                SnakeCaseConstants.CommitterName -> JsonUtils.toBsonValue(committer_name),
                SnakeCaseConstants.AuthorName -> JsonUtils.toBsonValue(author_name),
                SnakeCaseConstants.AuthoredDate -> JsonUtils.toBsonValue(authored_date),
                SnakeCaseConstants.Stats -> stats.toBsonValue(),
                SnakeCaseConstants.Refs -> JsonUtils.toBsonValue(refs)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.CommitId -> JsonUtils.toJsonValue(commit_id),
                SnakeCaseConstants.ParentIds -> JsonUtils.toJsonValue(parent_ids),
                SnakeCaseConstants.CommitterName -> JsonUtils.toJsonValue(committer_name),
                SnakeCaseConstants.AuthorName -> JsonUtils.toJsonValue(author_name),
                SnakeCaseConstants.AuthoredDate -> JsonUtils.toJsonValue(authored_date),
                SnakeCaseConstants.Stats -> stats.toJsValue(),
                SnakeCaseConstants.Refs -> JsonUtils.toJsonValue(refs)
            )
        )
    }
}

object ProjectCommitData {
    def fromCommitSchema(
        commitSchema: CommitSchema,
        numberOfFiles: Int,
        additions: Int,
        deletions: Int
    ): ProjectCommitData = {
        ProjectCommitData(
            commit_id = commitSchema.commit_hash,
            parent_ids = commitSchema.parents,
            committer_name = commitSchema.committer,
            author_name = commitSchema.author,
            authored_date =  commitSchema.author_date,
            stats = ProjectCommitStats(numberOfFiles, additions, deletions),
            refs = commitSchema.branches
        )
    }
}
