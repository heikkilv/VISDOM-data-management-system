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
