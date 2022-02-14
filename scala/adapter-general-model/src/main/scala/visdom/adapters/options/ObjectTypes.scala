package visdom.adapters.options

import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.authors.GitlabAuthor


object ObjectTypes {
    val OriginTypes: Set[String] = Set(GitlabOrigin.GitlabOriginType)
    val EventTypes: Set[String] = Set(CommitEvent.CommitEventType)
    val ArtifactTypes: Set[String] = Set(FileArtifact.FileArtifactType)
    val AuthorTypes: Set[String] = Set(GitlabAuthor.GitlabAuthorType)
}
