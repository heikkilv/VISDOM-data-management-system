package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.adapters.course.schemas.ExercisePointsSchema


final case class ExercisePointsOutput(
    id: Int,
    name: NameOutput,
    url: String,
    best_submission: Option[String],
    submissions: Seq[String],
    submissions_with_points: Seq[SubmissionPointsOutput],
    difficulty: String,
    max_points: Int,
    points_to_pass: Int,
    submission_count: Int,
    points: Int,
    passed: Boolean,
    official: Boolean
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
            SnakeCaseConstants.Name -> JsonUtils.toJsonValue(name),
            SnakeCaseConstants.Url -> JsonUtils.toJsonValue(url),
            SnakeCaseConstants.BestSubmission -> JsonUtils.toJsonValue(best_submission),
            SnakeCaseConstants.Submissions -> JsonUtils.toJsonValue(submissions),
            SnakeCaseConstants.SubmissionsWithPoints -> JsonUtils.toJsonValue(submissions_with_points),
            SnakeCaseConstants.Difficulty -> JsonUtils.toJsonValue(difficulty),
            SnakeCaseConstants.MaxPoints -> JsonUtils.toJsonValue(max_points),
            SnakeCaseConstants.PointsToPass -> JsonUtils.toJsonValue(points_to_pass),
            SnakeCaseConstants.SubmissionCount -> JsonUtils.toJsonValue(submission_count),
            SnakeCaseConstants.Points -> JsonUtils.toJsonValue(points),
            SnakeCaseConstants.Passed -> JsonUtils.toJsonValue(passed),
            SnakeCaseConstants.Official -> JsonUtils.toJsonValue(official)
        )
    }
}

object ExercisePointsOutput {
    def fromExercisePointsSchema(exercisePointsSchema: ExercisePointsSchema): ExercisePointsOutput = {
        ExercisePointsOutput(
            id = exercisePointsSchema.id,
            name = NameOutput.fromNameSchema(exercisePointsSchema.name),
            url = exercisePointsSchema.url,
            best_submission = exercisePointsSchema.best_submission,
            submissions = exercisePointsSchema.submissions,
            submissions_with_points = exercisePointsSchema.submissions_with_points.map(
                submission => SubmissionPointsOutput.fromSubmissionPointsSchema(submission)
            ),
            difficulty = exercisePointsSchema.difficulty,
            max_points = exercisePointsSchema.max_points,
            points_to_pass = exercisePointsSchema.points_to_pass,
            submission_count = exercisePointsSchema.submission_count,
            points = exercisePointsSchema.points,
            passed = exercisePointsSchema.passed,
            official = exercisePointsSchema.official
        )
    }
}
