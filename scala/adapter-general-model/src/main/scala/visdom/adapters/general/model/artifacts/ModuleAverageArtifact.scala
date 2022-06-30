// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.artifacts

import visdom.adapters.general.model.artifacts.data.ModuleAverageData
import visdom.adapters.general.model.artifacts.states.ModuleAverageState
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.metadata.CourseMetadata
import visdom.adapters.general.model.metadata.ModuleMetadata
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.schemas.CourseLinksSchema
import visdom.adapters.general.schemas.CourseSchema
import visdom.adapters.general.schemas.ModuleAverageSchema
import visdom.utils.GeneralUtils


class ModuleAverageArtifact(
    moduleAverageSchema: ModuleAverageSchema,
    cumulativeValues: ModuleAverageSchema,
    courseSchema: CourseSchema,
    moduleIds: Seq[Int],
    updateTime: String
)
extends Artifact {
    def getType: String = ModuleAverageArtifact.ModuleAverageArtifactType

    val origin: ItemLink = ItemLink(
        AplusOrigin.getId(courseSchema.host_name, courseSchema.id),
        AplusOrigin.AplusOriginType
    )


    val name: String = s"Week ${moduleAverageSchema.module_number} averages - grade ${moduleAverageSchema.grade}"
    val description: String =
            s"Week ${moduleAverageSchema.module_number} averages for grade ${moduleAverageSchema.grade} " +
            s"in course ${courseSchema.id}"

    val state: String = updateTime match {
        case dateString: String if dateString < courseSchema.starting_time => ModuleAverageState.NotStarted
        case dateString: String if dateString > courseSchema.ending_time => ModuleAverageState.Finished
        case _ => ModuleAverageState.Active
    }

    val data: ModuleAverageData =
        ModuleAverageData.fromModuleAverageSchema(moduleAverageSchema, cumulativeValues, courseSchema.id)

    val id: String = ModuleAverageArtifact.getId(origin.id, data.module_number, data.grade)

    // add linked course and module metadata as related constructs
    addRelatedConstructs(
        (
            moduleIds.map(
                moduleId => ItemLink(
                    ModuleMetadata.getId(origin.id, moduleId),
                    ModuleMetadata.ModuleMetadataType
                )
            )
        ) :+
        ItemLink(
            CourseMetadata.getId(origin.id, data.course_id),
            CourseMetadata.CourseMetadataType
        )
    )
}

object ModuleAverageArtifact {
    final val ModuleAverageArtifactType: String = "module_average"

    def getId(originId: String, moduleNumber: Int, grade: Int): String = {
        GeneralUtils.getUuid(originId, ModuleAverageArtifactType, moduleNumber.toString(), grade.toString())
    }
}
