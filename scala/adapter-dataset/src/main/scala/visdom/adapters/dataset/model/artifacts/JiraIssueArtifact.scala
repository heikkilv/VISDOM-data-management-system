package visdom.adapters.dataset.model.artifacts

import visdom.adapters.dataset.model.artifacts.data.JiraIssueData
import visdom.adapters.dataset.model.origins.ProjectOrigin
import visdom.adapters.dataset.schemas.JiraIssueSchema
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
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
    // NOTE: all files use the same state for now
    val state: String = jiraIssueSchema.status
    val data: JiraIssueData = JiraIssueData.fromIssueSchema(jiraIssueSchema)

    val id: String = JiraIssueArtifact.getId(origin.id, jiraIssueSchema.key)
}

object JiraIssueArtifact {
    final val JiraIssueArtifactType: String = "jira_issue"

    def getId(originId: String, issueKey: String): String = {
        GeneralUtils.getUuid(originId, JiraIssueArtifactType, issueKey)
    }
}
