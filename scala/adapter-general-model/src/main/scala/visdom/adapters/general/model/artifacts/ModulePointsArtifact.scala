package visdom.adapters.general.model.artifacts

import visdom.adapters.general.model.artifacts.data.ModulePointsData
import visdom.adapters.general.model.artifacts.states.PointsState
import visdom.adapters.general.model.authors.AplusAuthor
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.metadata.ModuleMetadata
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.schemas.ModuleNumbersSchema
import visdom.adapters.general.schemas.ModuleSchema
import visdom.adapters.general.schemas.PointsModuleSchema
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils


class ModulePointsArtifact(
    modulePointsSchema: PointsModuleSchema,
    moduleSchema: ModuleSchema,
    userId: Int,
    exerciseCount: Int,
    commitCount: Int,
    cumulativeValues: ModuleNumbersSchema,
    updateTime: String
)
extends Artifact {
    def getType: String = ModulePointsArtifact.ModulePointsArtifactType

    val origin: ItemLink =
        new AplusOrigin(
            moduleSchema.host_name,
            moduleSchema.course_id,
            None
        ).link

    val name: String = s"Module ${modulePointsSchema.id} points for ${userId}"
    val description: String = s"Module ${modulePointsSchema.name.raw} points for ${userId}"

    val state: String = updateTime match {
        case dateString: String if dateString < moduleSchema.metadata.other
            .map(other => other.start_date)
            .getOrElse(ModulePointsArtifact.DefaultStartTime) => PointsState.NotStarted
        case dateString: String if dateString > moduleSchema.metadata.other
            .map(other => other.end_date)
            .getOrElse(ModulePointsArtifact.DefaultEndTime) => PointsState.Finished
        case _ => modulePointsSchema.passed match {
            case true => PointsState.Passed
            case false => PointsState.Active
        }
    }

    val data: ModulePointsData = ModulePointsData.fromPointsSchema(
        modulePointsSchema,
        userId,
        exerciseCount,
        commitCount,
        cumulativeValues
    )

    val id: String = ModulePointsArtifact.getId(origin.id, data.module_id, data.user_id)

    // add the user and related module metadata, course points artifact,
    // and exercise points artifacts as related constructs
    addRelatedConstructs(
        Seq(
            ItemLink(
                AplusAuthor.getId(AplusOrigin.getId(moduleSchema.host_name), data.user_id),
                AplusAuthor.AplusAuthorType
            ),
            ItemLink(
                ModuleMetadata.getId(origin.id, data.module_id),
                ModuleMetadata.ModuleMetadataType
            ),
            ItemLink(
                CoursePointsArtifact.getId(origin.id, moduleSchema.course_id, data.user_id),
                CoursePointsArtifact.CoursePointsArtifactType
            )
        ) ++
        modulePointsSchema.exercises.map(
            exercisePointsSchema =>
                ItemLink(
                    ExercisePointsArtifact.getId(origin.id, exercisePointsSchema.id, data.user_id),
                    ExercisePointsArtifact.ExercisePointsArtifactType
                )
        )
    )
}

object ModulePointsArtifact {
    final val ModulePointsArtifactType: String = "module_points"

    final val DefaultStartTime: String = "1970-01-01T00:00:00.000Z"
    final val DefaultEndTime: String = "2050-01-01T00:00:00.000Z"

    def getId(originId: String, moduleId: Int, userId: Int): String = {
        GeneralUtils.getUuid(originId, ModulePointsArtifactType, moduleId.toString(), userId.toString())
    }
}
