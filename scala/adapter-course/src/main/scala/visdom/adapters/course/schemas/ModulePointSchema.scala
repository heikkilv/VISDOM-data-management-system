package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.TupleUtils.toOption
import visdom.utils.WartRemoverConstants


final case class ModulePointSchema(
    id: Int,
    name: NameSchema,
    max_points: Int,
    points_to_pass: Int,
    submission_count: Int,
    points: Int,
    points_by_difficulty: Option[ModulePointDifficultySchema],
    passed: Boolean,
    exercises: Seq[ExercisePointsSchema]
)
extends BaseSchema

object ModulePointSchema extends BaseSchemaTrait[ModulePointSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.Name, false),
        FieldDataType(SnakeCaseConstants.MaxPoints, false),
        FieldDataType(SnakeCaseConstants.PointsToPass, false),
        FieldDataType(SnakeCaseConstants.SubmissionCount, false),
        FieldDataType(SnakeCaseConstants.Points, false),
        FieldDataType(SnakeCaseConstants.PointsByDifficulty, true),
        FieldDataType(SnakeCaseConstants.Passed, false),
        FieldDataType(SnakeCaseConstants.Exercises, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[ModulePointSchema] = {
        val (
            idOption,
            nameOption,
            maxPointsOption,
            pointsToPassOption,
            submissionCountOption,
            pointsOption,
            pointsByDifficultyOption,
            passedOption,
            exercisesOption,
        ) = valueOptions.toTuple9

        toOption(
            (
                idOption,
                nameOption,
                maxPointsOption,
                pointsToPassOption,
                submissionCountOption,
                pointsOption,
                passedOption,
                exercisesOption,
            ),
            (
                (value: Any) => toIntOption(value),
                (value: Any) => NameSchema.fromAny(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => toBooleanOption(value),
                (value: Any) => toSeqOption(value, ExercisePointsSchema.fromAny)
            )
        ) match {
            case Some((
                id: Int,
                name: NameSchema,
                maxPoints: Int,
                pointsToPass: Int,
                submissionCount: Int,
                points: Int,
                passed: Boolean,
                exercises: Seq[ExercisePointsSchema]
            )) =>
                Some(
                    ModulePointSchema(
                        id,
                        name,
                        maxPoints,
                        pointsToPass,
                        submissionCount,
                        points,
                        ModulePointDifficultySchema.fromAny(pointsByDifficultyOption),
                        passed,
                        exercises
                    )
                )
            case _ => None
        }
    }
}
