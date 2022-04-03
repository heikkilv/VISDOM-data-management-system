package visdom.adapters.dataset.model.artifacts

import visdom.adapters.dataset.model.artifacts.data.SonarMeasuresData
import visdom.adapters.dataset.model.origins.ProjectOrigin
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.dataset.schemas.SonarMeasuresSchema
import visdom.utils.GeneralUtils


class SonarMeasuresArtifact(
    sonarMeasuresSchema: SonarMeasuresSchema,
    datasetName: String
)
extends Artifact {
    def getType: String = SonarMeasuresArtifact.SonarMeasuresArtifactType

    val origin: ItemLink = ItemLink(
        ProjectOrigin.getId(datasetName, sonarMeasuresSchema.project_id),
        ProjectOrigin.ProjectOriginType
    )

    val name: String = sonarMeasuresSchema.analysis_key
    val description: String =
        s"Measures ${sonarMeasuresSchema.analysis_key} for project ${sonarMeasuresSchema.project_id}"
    val state: String = sonarMeasuresSchema.alert_status
    val data: SonarMeasuresData = SonarMeasuresData.fromMeasuresSchema(sonarMeasuresSchema)

    val id: String = SonarMeasuresArtifact.getId(origin.id, sonarMeasuresSchema.analysis_key)
}

object SonarMeasuresArtifact {
    final val SonarMeasuresArtifactType: String = "sonar_measures"

    def getId(originId: String, analysisKey: String): String = {
        GeneralUtils.getUuid(originId, SonarMeasuresArtifactType, analysisKey)
    }
}
