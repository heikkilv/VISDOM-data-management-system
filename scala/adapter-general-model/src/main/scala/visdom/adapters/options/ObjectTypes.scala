package visdom.adapters.options

import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.artifacts.PipelineReportArtifact
import visdom.adapters.general.model.authors.CommitAuthor
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.Origin
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.utils.CommonConstants
import visdom.utils.SnakeCaseConstants


object ObjectTypes {
    final val TargetTypeAll: String = "All"

    val TargetTypeOrigin: String = Origin.OriginType
    val TargetTypeEvent: String = Event.EventType
    val TargetTypeAuthor: String = Author.AuthorType
    val TargetTypeArtifact: String = Artifact.ArtifactType

    val OriginTypes: Set[String] = Set(
        GitlabOrigin.GitlabOriginType
    )
    val EventTypes: Set[String] = Set(
        CommitEvent.CommitEventType,
        PipelineEvent.PipelineEventType,
        PipelineJobEvent.PipelineJobEventType
    )
    val AuthorTypes: Set[String] = Set(
        CommitAuthor.CommitAuthorType,
        GitlabAuthor.GitlabAuthorType
    )
    val ArtifactTypes: Set[String] = Set(
        FileArtifact.FileArtifactType,
        PipelineReportArtifact.PipelineReportArtifactType
    )

    val objectTypes: Map[String, Set[String]] = Map(
        TargetTypeOrigin -> OriginTypes,
        TargetTypeEvent -> EventTypes,
        TargetTypeAuthor -> AuthorTypes,
        TargetTypeArtifact -> ArtifactTypes
    )

    def getTargetType(objectType: String): Option[String] = {
        objectType match {
            case targetType: String if OriginTypes.contains(targetType) => Some(TargetTypeOrigin)
            case targetType: String if EventTypes.contains(targetType) => Some(TargetTypeEvent)
            case targetType: String if AuthorTypes.contains(targetType) => Some(TargetTypeAuthor)
            case targetType: String if ArtifactTypes.contains(targetType) => Some(TargetTypeArtifact)
            case _ => None
        }
    }

    final val BooleanType: String = "boolean"
    final val DoubleType: String = "double"
    final val IntType: String = "int"
    final val StringType: String = "string"
    final val DefaultAttributeType: String = StringType

    private def toName(attributeNames: String*): String = {
        attributeNames.mkString(CommonConstants.Dot)
    }

    // The default attribute type is String => only non-string attributes should be listed here
    val attributeTypes: Map[String, Map[String, String]] = Map(
        GitlabOrigin.GitlabOriginType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ProjectId) -> IntType
        ),
        CommitEvent.CommitEventType -> Map(
            SnakeCaseConstants.Duration -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Additions) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Deletions) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Stats, SnakeCaseConstants.Total) -> IntType
        ),
        PipelineEvent.PipelineEventType -> Map(
            SnakeCaseConstants.Duration -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.PipelineId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ProjectId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.QueuedDuration) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Tag) -> BooleanType
        ),
        PipelineJobEvent.PipelineJobEventType -> Map(
            SnakeCaseConstants.Duration -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.JobId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ProjectId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.UserId) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.Tag) -> BooleanType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.QueuedDuration) -> DoubleType
        ),
        FileArtifact.FileArtifactType -> Map.empty,
        PipelineReportArtifact.PipelineReportArtifactType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.TotalTime) -> DoubleType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.TotalCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.SuccessCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.FailedCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.SkippedCount) -> IntType,
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.ErrorCount) -> IntType
        ),
        CommitAuthor.CommitAuthorType -> Map.empty,
        GitlabAuthor.GitlabAuthorType -> Map(
            toName(SnakeCaseConstants.Data, SnakeCaseConstants.UserId) -> IntType
        )
    )

    def getAttributeType(objectType: String, attributeName: String): String = {
        attributeTypes.get(objectType) match {
            case Some(attributeMap: Map[String, String]) => attributeMap.get(attributeName) match {
                case Some(attributeType: String) => attributeType
                case None => DefaultAttributeType
            }
            case None => DefaultAttributeType
        }
    }
}
