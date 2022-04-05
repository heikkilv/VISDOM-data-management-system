package visdom.adapters.dataset.model.artifacts.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.adapters.dataset.schemas.JiraIssueSchema


final case class JiraIssueData(
    issue_key: String,
    `type`: String,
    priority: String,
    resolution: Option[String],
    reporter: String,
    creator: String,
    assignee: Option[String],
    time_original_estimate: Option[Double],
    time_spent: Option[Double],
    time_estimate: Option[Double],
    creation_date: String,
    resolution_date: Option[String],
    update_date: String,
    due_date: Option[String],
    progress_percent: Option[Double],
    commit_id: Option[String],
    commit_date: Option[String]
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.IssueKey -> JsonUtils.toBsonValue(issue_key),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(`type`),
                SnakeCaseConstants.Priority -> JsonUtils.toBsonValue(priority),
                SnakeCaseConstants.Resolution -> JsonUtils.toBsonValue(resolution),
                SnakeCaseConstants.Reporter -> JsonUtils.toBsonValue(reporter),
                SnakeCaseConstants.Creator -> JsonUtils.toBsonValue(creator),
                SnakeCaseConstants.Assignee -> JsonUtils.toBsonValue(assignee),
                SnakeCaseConstants.TimeOriginalEstimate -> JsonUtils.toBsonValue(time_original_estimate),
                SnakeCaseConstants.TimeSpent -> JsonUtils.toBsonValue(time_spent),
                SnakeCaseConstants.TimeEstimate -> JsonUtils.toBsonValue(time_estimate),
                SnakeCaseConstants.CreationDate -> JsonUtils.toBsonValue(creation_date),
                SnakeCaseConstants.ResolutionDate -> JsonUtils.toBsonValue(resolution_date),
                SnakeCaseConstants.UpdateDate -> JsonUtils.toBsonValue(update_date),
                SnakeCaseConstants.DueDate -> JsonUtils.toBsonValue(due_date),
                SnakeCaseConstants.ProgressPercent -> JsonUtils.toBsonValue(progress_percent),
                SnakeCaseConstants.CommitId -> JsonUtils.toBsonValue(commit_id),
                SnakeCaseConstants.CommitDate -> JsonUtils.toBsonValue(commit_date)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.IssueKey -> JsonUtils.toJsonValue(issue_key),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(`type`),
                SnakeCaseConstants.Priority -> JsonUtils.toJsonValue(priority),
                SnakeCaseConstants.Resolution -> JsonUtils.toJsonValue(resolution),
                SnakeCaseConstants.Reporter -> JsonUtils.toJsonValue(reporter),
                SnakeCaseConstants.Creator -> JsonUtils.toJsonValue(creator),
                SnakeCaseConstants.Assignee -> JsonUtils.toJsonValue(assignee),
                SnakeCaseConstants.TimeOriginalEstimate -> JsonUtils.toJsonValue(time_original_estimate),
                SnakeCaseConstants.TimeSpent -> JsonUtils.toJsonValue(time_spent),
                SnakeCaseConstants.TimeEstimate -> JsonUtils.toJsonValue(time_estimate),
                SnakeCaseConstants.CreationDate -> JsonUtils.toJsonValue(creation_date),
                SnakeCaseConstants.ResolutionDate -> JsonUtils.toJsonValue(resolution_date),
                SnakeCaseConstants.UpdateDate -> JsonUtils.toJsonValue(update_date),
                SnakeCaseConstants.DueDate -> JsonUtils.toJsonValue(due_date),
                SnakeCaseConstants.ProgressPercent -> JsonUtils.toJsonValue(progress_percent),
                SnakeCaseConstants.CommitId -> JsonUtils.toJsonValue(commit_id),
                SnakeCaseConstants.CommitDate -> JsonUtils.toJsonValue(commit_date)
            )
        )
    }
}

object JiraIssueData {
    def fromIssueSchema(jiraIssueSchema: JiraIssueSchema): JiraIssueData = {
        JiraIssueData(
            issue_key = jiraIssueSchema.key,
            `type` = jiraIssueSchema.`type`,
            priority = jiraIssueSchema.priority,
            resolution = jiraIssueSchema.resolution,
            reporter = jiraIssueSchema.reporter,
            creator = jiraIssueSchema.creator_name,
            assignee = jiraIssueSchema.assignee,
            time_original_estimate = jiraIssueSchema.time_original_estimate,
            time_spent = jiraIssueSchema.time_spent,
            time_estimate = jiraIssueSchema.time_estimate,
            creation_date = jiraIssueSchema.creation_date,
            resolution_date = jiraIssueSchema.resolution_date,
            update_date = jiraIssueSchema.update_date,
            due_date = jiraIssueSchema.due_date,
            progress_percent = jiraIssueSchema.progress_percent,
            commit_id = jiraIssueSchema.hash,
            commit_date = jiraIssueSchema.commit_date
        )
    }
}
