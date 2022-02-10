package visdom.adapters.general.model.artifacts

import visdom.adapters.general.model.artifacts.data.FileData
import visdom.adapters.general.model.artifacts.states.FileState
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.schemas.FileSchema
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils


class FileArtifact(
    fileSchema: FileSchema
)
extends Artifact {
    def getType: String = FileArtifact.FileArtifactType

    val origin: ItemLink =
        new GitlabOrigin(
            fileSchema.host_name,
            fileSchema.group_name,
            fileSchema.project_name
        ).link

    val name: String = fileSchema.name
    // use the full file name including the path as the description
    val description: String = fileSchema.path
    // NOTE: all files use the same state for now
    val state: FileState = FileState.FileExists
    val data: FileData = FileData.fromFileSchema(fileSchema)

    val id: String = FileArtifact.getId(origin.id, description)

    // add the parent folder as related artifact
    // NOTE: the folder contents could also be added as related artifacts
    GeneralUtils.getUpperFolder(description) match {
        case parentFolder: String if parentFolder != CommonConstants.EmptyString => addRelatedConstructs(
            Seq(
                ItemLink(
                    id = FileArtifact.getId(origin.id, parentFolder),
                    linkType = FileArtifact.FileArtifactType
                )
            )
        )
        case _ =>
    }

    // add linked commits as related events
    addRelatedEvents(
        data.commits.map(
            commitId => ItemLink(
                id = CommitEvent.getId(origin.id, commitId),
                linkType = CommitEvent.CommitEventType
            )
        )
    )
}

object FileArtifact {
    final val FileArtifactType: String = "file"

    def getId(originId: String, filePath: String): String = {
        GeneralUtils.getUuid(originId, FileArtifactType, filePath)
    }

    def fromFileSchema(fileSchema: FileSchema): FileArtifact = {
        new FileArtifact(fileSchema)
    }
}
