package visdom.adapters.options

import visdom.adapters.dataset.model.artifacts.JiraIssueArtifact
import visdom.adapters.dataset.model.artifacts.SonarMeasuresArtifact
import visdom.adapters.dataset.model.authors.UserAuthor
import visdom.adapters.dataset.model.events.ProjectCommitEvent
import visdom.adapters.dataset.model.origins.ProjectOrigin
import visdom.utils.SnakeCaseConstants


object DatasetObjectTypes extends ObjectTypesTrait {
    val OriginTypes: Set[String] = ObjectTypes.OriginTypes ++ Set(
        ProjectOrigin.ProjectOriginType
    )
    val EventTypes: Set[String] = ObjectTypes.EventTypes ++ Set(
        ProjectCommitEvent.ProjectCommitEventType
    )
    val AuthorTypes: Set[String] = ObjectTypes.AuthorTypes ++ Set(
        UserAuthor.UserAuthorType
    )
    val ArtifactTypes: Set[String] = ObjectTypes.ArtifactTypes ++ Set(
        JiraIssueArtifact.JiraIssueArtifactType,
        SonarMeasuresArtifact.SonarMeasuresArtifactType
    )
    val MetadataTypes: Set[String] = ObjectTypes.MetadataTypes

    // The default attribute type is String => only non-string attributes should be listed here
    private val newAttributeTypes: Map[String, Map[String, String]] = Map(
        ProjectOrigin.ProjectOriginType -> Map.empty,
        ProjectCommitEvent.ProjectCommitEventType -> Map(
            SnakeCaseConstants.Duration -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Additions) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Deletions) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Total) -> IntType
        ),
        JiraIssueArtifact.JiraIssueArtifactType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.TimeOriginalEstimate) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.TimeSpent) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.TimeEstimate) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ProgressPercent) -> DoubleType
        ),
        SonarMeasuresArtifact.SonarMeasuresArtifactType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Lines) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.CommentLines) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Files) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Bugs) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Vulnerabilities) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.CodeSmells) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.OpenIssues) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Duplications, SnakeCaseConstants.Lines) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Duplications, SnakeCaseConstants.Blocks) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Duplications, SnakeCaseConstants.Files) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Violations, SnakeCaseConstants.Blocker) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Violations, SnakeCaseConstants.Critical) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Violations, SnakeCaseConstants.Major) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Violations, SnakeCaseConstants.Minor) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Violations, SnakeCaseConstants.Info) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Violations, SnakeCaseConstants.Total) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Ratings, SnakeCaseConstants.Reliability) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Ratings, SnakeCaseConstants.Security) -> IntType
        ),
        UserAuthor.UserAuthorType -> Map.empty
    )

    val attributeTypes: Map[String, Map[String, String]] = ObjectTypes.attributeTypes ++ newAttributeTypes
}
