package visdom.adapters.options

import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.model.artifacts.JiraIssueArtifact
import visdom.adapters.dataset.model.artifacts.SonarMeasuresArtifact
import visdom.adapters.dataset.model.authors.UserAuthor
import visdom.adapters.dataset.model.events.ProjectCommitEvent
import visdom.adapters.dataset.model.origins.ProjectOrigin
import visdom.utils.SnakeCaseConstants


object DatasetObjectTypes extends ObjectTypesTrait {
    val OriginTypes: Set[String] = Set(
        ProjectOrigin.ProjectOriginType
    ) ++ (
        AdapterValues.onlyDataset match {
            case true => Set.empty
            case false => ObjectTypes.OriginTypes
        }
    )

    val EventTypes: Set[String] = Set(
        ProjectCommitEvent.ProjectCommitEventType
    ) ++ (
        AdapterValues.onlyDataset match {
            case true => Set.empty
            case false => ObjectTypes.EventTypes
        }
    )

    val AuthorTypes: Set[String] = Set(
        UserAuthor.UserAuthorType
    ) ++ (
        AdapterValues.onlyDataset match {
            case true => Set.empty
            case false => ObjectTypes.AuthorTypes
        }
    )

    val ArtifactTypes: Set[String] =  Set(
        JiraIssueArtifact.JiraIssueArtifactType,
        SonarMeasuresArtifact.SonarMeasuresArtifactType
    ) ++ (
        AdapterValues.onlyDataset match {
            case true => Set.empty
            case false => ObjectTypes.ArtifactTypes
        }
    )

    val MetadataTypes: Set[String] = AdapterValues.onlyDataset match {
        case true => Set.empty
        case false => ObjectTypes.MetadataTypes
    }

    val objectTypes: Map[String, Set[String]] = Map(
        TargetTypeOrigin -> OriginTypes,
        TargetTypeEvent -> EventTypes,
        TargetTypeAuthor -> AuthorTypes,
        TargetTypeArtifact -> ArtifactTypes,
        TargetTypeMetadata -> MetadataTypes
    )

    // The default attribute type is String => only non-string attributes should be listed here
    private val newAttributeTypes: Map[String, Map[String, String]] = Map(
        ProjectOrigin.ProjectOriginType -> Map.empty,
        ProjectCommitEvent.ProjectCommitEventType -> Map(
            SnakeCaseConstants.Duration -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Additions) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Deletions) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Files) -> IntType
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
        UserAuthor.UserAuthorType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Commits) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Issues) -> IntType
        )
    )

    val attributeTypes: Map[String, Map[String, String]] = newAttributeTypes ++ (
        AdapterValues.onlyDataset match {
            case true => Map.empty
            case false => ObjectTypes.attributeTypes
        }
    )
}
