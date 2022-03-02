package visdom.adapters.options

import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.artifacts.PipelineReportArtifact
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.Origin
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.origins.GitlabOrigin


object ObjectTypes {
    final val TargetTypeAll: String = "All"

    val TargetTypeOrigin: String = Origin.OriginType
    val TargetTypeEvent: String = Event.EventType
    val TargetTypeAuthor: String = Author.AuthorType
    val TargetTypeArtifact: String = Artifact.ArtifactType

    val OriginTypes: Set[String] = Set(GitlabOrigin.GitlabOriginType)
    val EventTypes: Set[String] = Set(
        CommitEvent.CommitEventType,
        PipelineEvent.PipelineEventType,
        PipelineJobEvent.PipelineJobEventType
    )
    val AuthorTypes: Set[String] = Set(GitlabAuthor.GitlabAuthorType)
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
}
