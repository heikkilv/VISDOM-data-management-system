// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.artifacts

import visdom.adapters.general.model.artifacts.data.ExercisePointsData
import visdom.adapters.general.model.artifacts.states.PointsState
import visdom.adapters.general.model.authors.AplusAuthor
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.base.LinkTrait
import visdom.adapters.general.model.events.SubmissionEvent
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
    relatedCommitEventLinks: Seq[LinkTrait],
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

    val data: ExercisePointsData = ExercisePointsData.fromPointsSchema(
        exercisePointsSchema,
        userId,
        relatedCommitEventLinks
    )

    val id: String = ExercisePointsArtifact.getId(origin.id, data.exercise_id, data.user_id)

    // add the user and related exercise metadata and module points artifact as related constructs
    addRelatedConstructs(
        Seq(
            ItemLink(
                AplusAuthor.getId(AplusOrigin.getId(exerciseSchema.host_name), data.user_id),
                AplusAuthor.AplusAuthorType
            ),
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

    // add submissions and commits as related events
    addRelatedEvents(
        data.submissions_with_points.map(
            submission => ItemLink(
                SubmissionEvent.getId(origin.id, submission.id),
                SubmissionEvent.SubmissionEventType
            )
        ) ++
        relatedCommitEventLinks
    )
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
        relatedCommitEventLinks: Seq[LinkTrait],
        updateTime: String
    ): ExercisePointsArtifact = {
        new ExercisePointsArtifact(
            exercisePointsSchema,
            exerciseSchema,
            additionalSchema,
            moduleId,
            userId,
            relatedCommitEventLinks,
            updateTime
        )
    }
}
