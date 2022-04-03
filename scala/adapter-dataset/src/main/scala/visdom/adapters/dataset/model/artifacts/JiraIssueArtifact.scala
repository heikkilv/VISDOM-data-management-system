package visdom.adapters.dataset.model.artifacts

import visdom.adapters.dataset.model.artifacts.data.JiraIssueData
import visdom.adapters.dataset.model.authors.UserAuthor
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.dataset.model.events.ProjectCommitEvent
import visdom.adapters.dataset.model.origins.ProjectOrigin
import visdom.adapters.dataset.schemas.JiraIssueSchema
import visdom.utils.GeneralUtils


class JiraIssueArtifact(
    jiraIssueSchema: JiraIssueSchema,
    datasetName: String
)
extends Artifact {
    def getType: String = JiraIssueArtifact.JiraIssueArtifactType

    val origin: ItemLink = ItemLink(
        ProjectOrigin.getId(datasetName, jiraIssueSchema.project_id),
        ProjectOrigin.ProjectOriginType
    )

    val name: String = jiraIssueSchema.summary
    val description: String = jiraIssueSchema.description
    val state: String = jiraIssueSchema.status
    val data: JiraIssueData = JiraIssueData.fromIssueSchema(jiraIssueSchema)

    val id: String = JiraIssueArtifact.getId(origin.id, jiraIssueSchema.key)

    // add linked users as related constructs
    addRelatedConstructs(
        (
            Seq(data.creator, data.reporter) ++
            (
                data.assignee match {
                    case Some(assignee: String) => Seq(assignee)
                    case None => Seq.empty
                }
            )
        )
        .distinct
        .map(
            username => ItemLink(
                UserAuthor.getId(ProjectOrigin.getId(datasetName), username),
                UserAuthor.UserAuthorType
            )
        )
    )

    // add linked commit as related event
    data.commit_id match {
        case Some(commitId: String) => addRelatedEvent(
            ItemLink(
                ProjectCommitEvent.getId(origin.id, commitId),
                ProjectCommitEvent.ProjectCommitEventType
            )
        )
        case None =>
    }
}

object JiraIssueArtifact {
    final val JiraIssueArtifactType: String = "jira_issue"

    def getId(originId: String, issueKey: String): String = {
        GeneralUtils.getUuid(originId, JiraIssueArtifactType, issueKey)
    }
}
