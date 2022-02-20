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
import visdom.adapters.general.schemas.FileSchema
import visdom.adapters.general.schemas.GitlabAuthorSchema
import visdom.adapters.results.BaseResultValue
import visdom.adapters.results.IdValue
import visdom.json.JsonUtils
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class ArtifactResult[ArtifactData <: Data](
    _id: String,
    id: String,
    `type`: String,
    name: String,
    description: String,
    state: String,
    origin: ItemLink,
    data: ArtifactData,
    related_constructs: Seq[ItemLink],
    related_events: Seq[ItemLink]
)
extends BaseResultValue
with IdValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toBsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(`type`),
                SnakeCaseConstants.Name -> JsonUtils.toBsonValue(name),
                SnakeCaseConstants.Description -> JsonUtils.toBsonValue(description),
                SnakeCaseConstants.State -> JsonUtils.toBsonValue(state),
                SnakeCaseConstants.Origin -> origin.toBsonValue(),
                SnakeCaseConstants.Data -> data.toBsonValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toBsonValue(related_constructs.map(link => link.toBsonValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toBsonValue(related_events.map(link => link.toBsonValue()))
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(`type`),
                SnakeCaseConstants.Name -> JsonUtils.toJsonValue(name),
                SnakeCaseConstants.Description -> JsonUtils.toJsonValue(description),
                SnakeCaseConstants.State -> JsonUtils.toJsonValue(state),
                SnakeCaseConstants.Origin -> origin.toJsValue(),
                SnakeCaseConstants.Data -> data.toJsValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toJsonValue(related_constructs.map(link => link.toJsValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toJsonValue(related_events.map(link => link.toJsValue()))
            )
        )
    }
}

object ArtifactResult {
    type FileArtifactResult = ArtifactResult[FileData]
    type GitlabAuthorResult = ArtifactResult[GitlabAuthorData]

    def fromArtifact[ArtifactData <: Data](
        artifact: Artifact,
        artifactData: ArtifactData
    ): ArtifactResult[ArtifactData] = {
        ArtifactResult(
            _id = artifact.id,
            id = artifact.id,
            `type` = artifact.getType,
            name = artifact.name,
            description = artifact.description,
            state = artifact.state,
            origin = artifact.origin,
            data = artifactData,
            related_constructs = artifact.relatedConstructs.map(link => ItemLink.fromLinkTrait(link)),
            related_events = artifact.relatedEvents.map(link => ItemLink.fromLinkTrait(link))
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
        fromArtifact(gitlabAuthor, gitlabAuthor.data)
    }

    def fromFileSchema(fileSchema: FileSchema): FileArtifactResult = {
        val fileArtifact: FileArtifact = new FileArtifact(fileSchema)
        fromArtifact(fileArtifact, fileArtifact.data)
    }
}
