package visdom.adapters.general.model.artifacts

import visdom.adapters.general.model.artifacts.data.ExercisePointsData
import visdom.adapters.general.model.artifacts.states.PointsState
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.metadata.ExerciseMetadata
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.schemas.ExerciseSchema
import visdom.adapters.general.schemas.ExerciseAdditionalSchema
import visdom.adapters.general.schemas.PointsExerciseSchema
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils


class ExercisePointsArtifact(
    exercisePointsSchema: PointsExerciseSchema,
    exerciseSchema: ExerciseSchema,
    additionalSchema: ExerciseAdditionalSchema,
    moduleId: Int,
    userId: Int,
    updateTime: String
)
extends Artifact {
    def getType: String = ExercisePointsArtifact.ExercisePointsArtifactType

    val origin: ItemLink =
        new AplusOrigin(
            exerciseSchema.host_name,
            exerciseSchema.course.id,
            None
        ).link

    val name: String = s"Exercise ${exercisePointsSchema.id} points for ${userId}"
    val description: String = s"Exercise ${exercisePointsSchema.name.raw} points for ${userId}"

    val state: String = updateTime match {
        case dateString: String if dateString < additionalSchema.start_date
            .getOrElse(ExercisePointsArtifact.DefaultStartTime) => PointsState.NotStarted
        case dateString: String if dateString > additionalSchema.end_date
            .getOrElse(ExercisePointsArtifact.DefaultEndTime) => PointsState.Finished
        case _ => exercisePointsSchema.passed match {
            case true => PointsState.Passed
            case false => PointsState.Active
        }
    }

    val data: ExercisePointsData = ExercisePointsData.fromPointsSchema(exercisePointsSchema, userId)

    val id: String = ModulePointsArtifact.getId(origin.id, data.exercise_id, data.user_id)

    // add related exercise metadata and module points artifact as related constructs
    addRelatedConstructs(
        Seq(
            ItemLink(
                ExerciseMetadata.getId(origin.id, data.exercise_id),
                ExerciseMetadata.ExerciseMetadataType
            ),
            ItemLink(
                ModulePointsArtifact.getId(origin.id, moduleId, userId),
                ModulePointsArtifact.ModulePointsArtifactType
            )
        )
    )

    // TODO: add links to submission events, aplus user
}

object ExercisePointsArtifact {
    final val ExercisePointsArtifactType: String = "exercise_points"

    final val DefaultStartTime: String = "1970-01-01T00:00:00.000Z"
    final val DefaultEndTime: String = "2050-01-01T00:00:00.000Z"

    def getId(originId: String, exerciseId: Int, userId: Int): String = {
        GeneralUtils.getUuid(originId, ExercisePointsArtifactType, exerciseId.toString(), userId.toString())
    }

    def fromPointsSchema(
        exercisePointsSchema: PointsExerciseSchema,
        exerciseSchema: ExerciseSchema,
        additionalSchema: ExerciseAdditionalSchema,
        moduleId: Int,
        userId: Int,
        updateTime: String
    ): ExercisePointsArtifact = {
        new ExercisePointsArtifact(
            exercisePointsSchema,
            exerciseSchema,
            additionalSchema,
            moduleId,
            userId,
            updateTime
        )
    }
}
