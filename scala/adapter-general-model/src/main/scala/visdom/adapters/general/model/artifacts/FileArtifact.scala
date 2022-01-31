package visdom.adapters.general.model.artifacts

import visdom.utils.GeneralUtils


object FileArtifact {
    final val FileArtifactType: String = "file"

    def getId(originId: String, filePath: String): String = {
        GeneralUtils.getUuid(originId, FileArtifactType, filePath)
    }
}
