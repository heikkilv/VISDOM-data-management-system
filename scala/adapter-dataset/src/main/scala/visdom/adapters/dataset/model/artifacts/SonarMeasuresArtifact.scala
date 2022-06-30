// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset.model.artifacts

import visdom.adapters.dataset.model.artifacts.data.SonarMeasuresData
import visdom.adapters.dataset.model.origins.ProjectOrigin
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.dataset.schemas.SonarMeasuresSchema
import visdom.utils.GeneralUtils


class SonarMeasuresArtifact(
    sonarMeasuresSchema: SonarMeasuresSchema,
    datasetName: String,
    relatedEvents: Seq[ItemLink]
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

    addRelatedEvents(relatedEvents)
}

object SonarMeasuresArtifact {
    final val SonarMeasuresArtifactType: String = "sonar_measures"

    def getId(originId: String, analysisKey: String): String = {
        GeneralUtils.getUuid(originId, SonarMeasuresArtifactType, analysisKey)
    }

    def getId(datasetName: String, projectId: String, analysisKey: String): String = {
        getId(
            originId = ProjectOrigin.getId(datasetName, projectId),
            analysisKey = analysisKey
        )
    }
}
