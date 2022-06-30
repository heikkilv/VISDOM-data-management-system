// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset.model.origins

import visdom.adapters.dataset.model.origins.data.ProjectData
import visdom.adapters.dataset.schemas.ProjectSchema
import visdom.adapters.general.model.base.Origin
import visdom.utils.GeneralUtils
import visdom.utils.CommonConstants


class ProjectOrigin(datasetName: String, projectDetails: Option[ProjectSchema])
extends Origin {
    def getType: String = ProjectOrigin.ProjectOriginType
    val source: String = datasetName
    val context: String = projectDetails match {
        case Some(schema: ProjectSchema) => schema.project_id
        case None => CommonConstants.EmptyString
    }

    override val id: String = ProjectOrigin.getId(source, context)
    val data: ProjectData = projectDetails match {
        case Some(schema: ProjectSchema) => ProjectData.fromProjectSchema(schema)
        case None => ProjectData.getEmpty()
    }
}

object ProjectOrigin {
    final val ProjectOriginType: String = "project"

    def getProjectOriginFromDataset(datasetName: String): ProjectOrigin = {
        new ProjectOrigin(datasetName, None)
    }

    def getId(datasetName: String): String = {
        getId(datasetName, CommonConstants.EmptyString)
    }

    def getId(datasetName: String, projectId: String): String = {
        GeneralUtils.getUuid(ProjectOriginType, datasetName, projectId)
    }
}
