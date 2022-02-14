package visdom.adapters.general.model.results

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.artifacts.data.FileData
import visdom.adapters.general.model.artifacts.states.FileState
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.authors.data.GitlabAuthorData
import visdom.adapters.general.model.authors.states.AuthorState
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.base.State
import visdom.adapters.general.schemas.FileSchema
import visdom.adapters.general.schemas.GitlabAuthorSchema
import visdom.adapters.results.BaseResultValue
import visdom.adapters.results.IdValue
import visdom.json.JsonUtils
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class ArtifactResult[ArtifactData <: Data, ArtifactState <: State](
    id: String,
    artifactType: String,
    name: String,
    description: String,
    state: ArtifactState,
    origin: ItemLink,
    data: ArtifactData,
    relatedConstructs: Seq[ItemLink],
    relatedEvents: Seq[ItemLink]
)
extends BaseResultValue
with IdValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toBsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(artifactType),
                SnakeCaseConstants.Name -> JsonUtils.toBsonValue(name),
                SnakeCaseConstants.Description -> JsonUtils.toBsonValue(description),
                SnakeCaseConstants.State -> state.toBsonValue(),
                SnakeCaseConstants.Origin -> origin.toBsonValue(),
                SnakeCaseConstants.Data -> data.toBsonValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toBsonValue(relatedConstructs.map(link => link.toBsonValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toBsonValue(relatedEvents.map(link => link.toBsonValue()))
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(artifactType),
                SnakeCaseConstants.Name -> JsonUtils.toJsonValue(name),
                SnakeCaseConstants.Description -> JsonUtils.toJsonValue(description),
                SnakeCaseConstants.State -> state.toJsValue(),
                SnakeCaseConstants.Origin -> origin.toJsValue(),
                SnakeCaseConstants.Data -> data.toJsValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toJsonValue(relatedConstructs.map(link => link.toJsValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toJsonValue(relatedEvents.map(link => link.toJsValue()))
            )
        )
    }
}

object ArtifactResult {
    type FileArtifactResult = ArtifactResult[FileData, FileState]
    type GitlabAuthorResult = ArtifactResult[GitlabAuthorData, AuthorState]

    def fromArtifact[ArtifactData <: Data, ArtifactState <: State](
        artifact: Artifact,
        artifactData: ArtifactData,
        artifactState: ArtifactState
    ): ArtifactResult[ArtifactData, ArtifactState] = {
        ArtifactResult(
            id = artifact.id,
            artifactType = artifact.getType,
            name = artifact.name,
            description = artifact.description,
            state = artifactState,
            origin = artifact.origin,
            data = artifactData,
            relatedConstructs = artifact.relatedConstructs.map(link => ItemLink.fromLinkTrait(link)),
            relatedEvents = artifact.relatedEvents.map(link => ItemLink.fromLinkTrait(link))
        )
    }

    def fromGitlabAuthorSchema(gitlabAuthorSchema: GitlabAuthorSchema): GitlabAuthorResult = {
        val gitlabAuthor: GitlabAuthor = new GitlabAuthor(
            authorName = gitlabAuthorSchema.committer_name,
            authorEmail = gitlabAuthorSchema.committer_email,
            hostName = gitlabAuthorSchema.host_name,
            authorDescription = None,
            userId = None,
            relatedCommitEventIds = gitlabAuthorSchema.related_commit_event_ids
        )
        fromArtifact(gitlabAuthor, gitlabAuthor.data, gitlabAuthor.state)
    }

    def fromFileSchema(fileSchema: FileSchema): FileArtifactResult = {
        val fileArtifact: FileArtifact = new FileArtifact(fileSchema)
        fromArtifact(fileArtifact, fileArtifact.data, fileArtifact.state)
    }
}
