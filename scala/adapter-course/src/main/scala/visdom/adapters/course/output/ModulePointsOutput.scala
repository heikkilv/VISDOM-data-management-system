package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.adapters.course.schemas.ExercisePointsSchema
import visdom.adapters.course.schemas.ModulePointSchema


final case class ModulePointsOutput(
    id: Int,
    name: NameOutput,
    max_points: Int,
    points_to_pass: Int,
    submission_count: Int,
    points: Int,
    points_by_difficulty: Option[PointsByDifficultyOutput],
    passed: Boolean,
    exercises: Seq[ExercisePointsOutput]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
            SnakeCaseConstants.Name -> JsonUtils.toJsonValue(name),
            SnakeCaseConstants.MaxPoints -> JsonUtils.toJsonValue(max_points),
            SnakeCaseConstants.PointsToPass -> JsonUtils.toJsonValue(points_to_pass),
            SnakeCaseConstants.SubmissionCount -> JsonUtils.toJsonValue(submission_count),
            SnakeCaseConstants.Points -> JsonUtils.toJsonValue(points),
            SnakeCaseConstants.PointsByDifficulty -> JsonUtils.toJsonValue(points_by_difficulty),
            SnakeCaseConstants.Passed -> JsonUtils.toJsonValue(passed),
            SnakeCaseConstants.Exercises -> JsonUtils.toJsonValue(exercises)
        )
    }
}

object ModulePointsOutput {
    def fromModulePointSchema(
        modulePointSchema: ModulePointSchema,
        exerciseIdOption: Option[Int]
    ): ModulePointsOutput = {
        val consideredExercises: Seq[ExercisePointsSchema] = exerciseIdOption match {
            case Some(exerciseId: Int) => modulePointSchema.exercises.filter(exercise => exercise.id == exerciseId)
            case None => modulePointSchema.exercises
        }

        ModulePointsOutput(
            id = modulePointSchema.id,
            name = NameOutput.fromNameSchema(modulePointSchema.name),
            max_points = modulePointSchema.max_points,
            points_to_pass = modulePointSchema.points_to_pass,
            submission_count = modulePointSchema.submission_count,
            points = modulePointSchema.points,
            points_by_difficulty =
                modulePointSchema.points_by_difficulty.map(
                    pointByDifficulty => PointsByDifficultyOutput.fromPointDifficultySchema(pointByDifficulty)
                ),
            passed = modulePointSchema.passed,
            exercises =
                consideredExercises.map(exercise => ExercisePointsOutput.fromExercisePointsSchema(exercise))
        )
    }
}
