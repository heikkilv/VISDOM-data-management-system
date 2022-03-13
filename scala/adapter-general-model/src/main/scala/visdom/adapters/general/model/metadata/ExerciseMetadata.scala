package visdom.adapters.general.model.metadata

import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.base.Metadata
import visdom.adapters.general.model.metadata.data.ExerciseData
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.schemas.ExerciseAdditionalSchema
import visdom.adapters.general.schemas.ExerciseSchema
import visdom.utils.GeneralUtils


class ExerciseMetadata(
    exerciseSchema: ExerciseSchema,
    exerciseAdditionalSchema: ExerciseAdditionalSchema
)
extends Metadata {
    def getType: String = CourseMetadata.CourseMetadataType

    val origin: ItemLink =
        new AplusOrigin(
            exerciseSchema.host_name,
            exerciseSchema.course.id,
            None
        ).link

    val name: String = exerciseSchema.display_name.en match {
        case Some(englishName: String) => englishName
        case None => exerciseSchema.display_name.fi match {
            case Some(finnishName: String) => finnishName
            case None => exerciseSchema.display_name.raw
        }
    }
    val description: String = exerciseSchema.display_name.raw

    val data: ExerciseData = ExerciseData.fromExerciseSchema(exerciseSchema, exerciseAdditionalSchema)

    val id: String = ExerciseMetadata.getId(origin.id, data.exercise_id)

    // add links to the related course and module metadata
    addRelatedConstructs(
        Seq(
            ItemLink(
                CourseMetadata.getId(origin.id, data.course_id),
                CourseMetadata.CourseMetadataType
            ),
            ItemLink(
                ModuleMetadata.getId(origin.id, data.module_id),
                ModuleMetadata.ModuleMetadataType
            )
        )
    )
}

object ExerciseMetadata {
    final val ExerciseMetadataType: String = "exercise"

    def getId(originId: String, exerciseId: Int): String = {
        GeneralUtils.getUuid(originId, ExerciseMetadataType, exerciseId.toString())
    }

    def fromExerciseSchema(
        exerciseSchema: ExerciseSchema,
        additionalSchema: ExerciseAdditionalSchema
    ): ExerciseMetadata = {
        new ExerciseMetadata(exerciseSchema, additionalSchema)
    }
}
