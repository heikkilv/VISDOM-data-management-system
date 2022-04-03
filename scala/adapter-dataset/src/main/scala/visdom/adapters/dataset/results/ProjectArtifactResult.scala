package visdom.adapters.dataset.results

import visdom.adapters.dataset.model.artifacts.JiraIssueArtifact
import visdom.adapters.dataset.model.artifacts.SonarMeasuresArtifact
import visdom.adapters.dataset.model.artifacts.data.JiraIssueData
import visdom.adapters.dataset.model.artifacts.data.SonarMeasuresData
import visdom.adapters.dataset.model.authors.UserAuthor
import visdom.adapters.dataset.model.authors.data.UserData
import visdom.adapters.general.model.results.ArtifactResult
import visdom.adapters.dataset.schemas.JiraIssueSchema
import visdom.adapters.dataset.schemas.SonarMeasuresSchema


object ProjectArtifactResult {
    type UserAuthorResult = ArtifactResult[UserData]

    type JiraIssueArtifactResult = ArtifactResult[JiraIssueData]
    type SonarMeasuresArtifactResult = ArtifactResult[SonarMeasuresData]

    def fromUsername(username: String, datasetName: String): UserAuthorResult = {
        val userAuthor: UserAuthor = new UserAuthor(
            username = username,
            datasetName = datasetName
        )
        ArtifactResult.fromArtifact(userAuthor, userAuthor.data)
    }

    def fromJiraIssueSchema(issueSchema: JiraIssueSchema, datasetName: String): JiraIssueArtifactResult = {
        val issueArtifact: JiraIssueArtifact = new JiraIssueArtifact(
            jiraIssueSchema = issueSchema,
            datasetName = datasetName
        )
        ArtifactResult.fromArtifact(issueArtifact, issueArtifact.data)
    }

    def fromSonarMeasuresSchema(
        measuresSchema: SonarMeasuresSchema,
        datasetName: String
    ): SonarMeasuresArtifactResult = {
        val measuresArtifact: SonarMeasuresArtifact = new SonarMeasuresArtifact(
            sonarMeasuresSchema = measuresSchema,
            datasetName = datasetName
        )
        ArtifactResult.fromArtifact(measuresArtifact, measuresArtifact.data)
    }
}
