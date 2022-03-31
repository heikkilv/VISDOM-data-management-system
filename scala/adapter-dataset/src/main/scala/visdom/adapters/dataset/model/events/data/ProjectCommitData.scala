package visdom.adapters.dataset.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.dataset.schemas.CommitChangeSchema
import visdom.adapters.dataset.schemas.CommitSchema
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.model.events.data.CommitStats
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ProjectCommitData(
    commit_id: String,
    parent_ids: Seq[String],
    committer_name: String,
    author_name: String,
    authored_date: String,
    stats: CommitStats,
    refs: Seq[String],
    files: Seq[String]
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
                SnakeCaseConstants.Refs -> JsonUtils.toBsonValue(refs),
                SnakeCaseConstants.Files -> JsonUtils.toBsonValue(files)
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
                SnakeCaseConstants.Refs -> JsonUtils.toJsonValue(refs),
                SnakeCaseConstants.Files -> JsonUtils.toJsonValue(files)
            )
        )
    }
}

object ProjectCommitData {
    def fromCommitSchema(
        commitSchema: CommitSchema,
        commitChangeSchemas: Seq[CommitChangeSchema]
    ): ProjectCommitData = {
        ProjectCommitData(
            commit_id = commitSchema.commit_hash,
            parent_ids = commitSchema.parents,
            committer_name = commitSchema.committer,
            author_name = commitSchema.author,
            authored_date =  commitSchema.author_date,
            stats = commitChangeSchemas
                .map(commitChange => (commitChange.lines_added, commitChange.lines_removed))
                .reduceOption((stats1, stats2) => (stats1._1 + stats2._1, stats1._2 + stats2._2))
                match {
                    case Some((additions: Int, deletions: Int)) =>
                        CommitStats(additions, deletions, additions + deletions)
                    case None => CommitStats.getEmpty()
                },
            refs = commitSchema.branches,
            files = commitChangeSchemas.map(commitChange => commitChange.file)
        )
    }
}
