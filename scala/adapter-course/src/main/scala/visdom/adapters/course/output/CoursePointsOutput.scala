package visdom.adapters.course.output

import spray.json.JsObject
import visdom.adapters.course.schemas.ModulePointSchema
import visdom.adapters.course.schemas.PointSchema
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class CoursePointsOutput(
    submission_count: Int,
    points: Int,
    points_by_difficulty: PointsByDifficultyOutput,
    modules: Seq[ModulePointsOutput]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.SubmissionCount -> JsonUtils.toJsonValue(submission_count),
            SnakeCaseConstants.Points -> JsonUtils.toJsonValue(points),
            SnakeCaseConstants.PointsByDifficulty -> JsonUtils.toJsonValue(points_by_difficulty),
            SnakeCaseConstants.Modules -> JsonUtils.toJsonValue(modules)
        )
    }
}

object CoursePointsOutput {
    def fromPointSchema(pointSchema: PointSchema, exerciseIdOption: Option[Int]): CoursePointsOutput = {
        val consideredModules: Seq[ModulePointSchema] = exerciseIdOption match {
            case Some(exerciseId: Int) =>
                pointSchema.modules
                    .filter(module => module.exercises.map(exercise => exercise.id).contains(exerciseId))
            case None => pointSchema.modules
        }

        CoursePointsOutput(
            submission_count = pointSchema.submission_count,
            points = pointSchema.points,
            points_by_difficulty =
                PointsByDifficultyOutput.fromPointDifficultySchema(pointSchema.points_by_difficulty),
            modules =
                consideredModules.map(module => ModulePointsOutput.fromModulePointSchema(module, exerciseIdOption))
        )
    }
}
