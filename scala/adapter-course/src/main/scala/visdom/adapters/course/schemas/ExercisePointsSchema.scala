package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class ExercisePointsSchema(
    id: Int,
    submissions_with_points: Seq[SubmissionPointsSchema]
)
extends BaseSchema

object ExercisePointsSchema extends BaseSchemaTrait[ExercisePointsSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.SubmissionsWithPoints, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[ExercisePointsSchema] = {
        toOption(
            valueOptions.toTuple2,
            (
                (value: Any) => toIntOption(value),
                (value: Any) => toSeqOption(value, SubmissionPointsSchema.fromAny)
            )
         ) match {
            case Some((id: Int, submissions_with_points: Seq[SubmissionPointsSchema])) =>
                Some(
                    ExercisePointsSchema(
                        id,
                        submissions_with_points
                    )
                )
            case _ => None
        }
    }
}
