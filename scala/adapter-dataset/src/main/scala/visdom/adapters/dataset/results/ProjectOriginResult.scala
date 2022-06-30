// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset.results

import visdom.adapters.dataset.model.origins.ProjectOrigin
import visdom.adapters.dataset.model.origins.data.ProjectData
import visdom.adapters.dataset.schemas.ProjectSchema
import visdom.adapters.general.model.results.OriginResult


object ProjectOriginResult {
    type ProjectOriginResult = OriginResult[ProjectData]

    def fromProjectSchema(datasetName: String, projectSchema: Option[ProjectSchema]): ProjectOriginResult = {
        val projectOrigin: ProjectOrigin = new ProjectOrigin(
            datasetName = datasetName,
            projectDetails = projectSchema
        )
        OriginResult.fromOrigin(projectOrigin, projectOrigin.data)
    }
}
