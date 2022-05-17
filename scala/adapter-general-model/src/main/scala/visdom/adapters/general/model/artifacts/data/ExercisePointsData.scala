package visdom.adapters.general.model.artifacts.data


import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.model.base.LinkTrait
import visdom.adapters.general.schemas.PointsExerciseSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class ExercisePointsData(
    exercise_id: Int,
    user_id: Int,
    submission_count: Int,
    commit_count: Int,
    points: Int,
    max_points: Int,
    passed: Boolean,
    official: Boolean,
    submissions: Seq[String],
    best_submission: Option[String],
    submissions_with_points: Seq[ExerciseSubmissionData]
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.ExerciseId -> JsonUtils.toBsonValue(exercise_id),
                SnakeCaseConstants.UserId -> JsonUtils.toBsonValue(user_id),
                SnakeCaseConstants.SubmissionCount -> JsonUtils.toBsonValue(submission_count),
                SnakeCaseConstants.CommitCount -> JsonUtils.toBsonValue(commit_count),
                SnakeCaseConstants.Points -> JsonUtils.toBsonValue(points),
                SnakeCaseConstants.MaxPoints -> JsonUtils.toBsonValue(max_points),
                SnakeCaseConstants.Passed -> JsonUtils.toBsonValue(passed),
                SnakeCaseConstants.Official -> JsonUtils.toBsonValue(official),
                SnakeCaseConstants.Submissions -> JsonUtils.toBsonValue(submissions),
                SnakeCaseConstants.BestSubmission -> JsonUtils.toBsonValue(best_submission),
                SnakeCaseConstants.SubmissionsWithPoints ->
                    JsonUtils.toBsonValue(submissions_with_points.map(submission => submission.toBsonValue()))
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.ExerciseId -> JsonUtils.toJsonValue(exercise_id),
                SnakeCaseConstants.UserId -> JsonUtils.toJsonValue(user_id),
                SnakeCaseConstants.SubmissionCount -> JsonUtils.toJsonValue(submission_count),
                SnakeCaseConstants.CommitCount -> JsonUtils.toJsonValue(commit_count),
                SnakeCaseConstants.Points -> JsonUtils.toJsonValue(points),
                SnakeCaseConstants.MaxPoints -> JsonUtils.toJsonValue(max_points),
                SnakeCaseConstants.Passed -> JsonUtils.toJsonValue(passed),
                SnakeCaseConstants.Official -> JsonUtils.toJsonValue(official),
                SnakeCaseConstants.Submissions -> JsonUtils.toJsonValue(submissions),
                SnakeCaseConstants.BestSubmission -> JsonUtils.toJsonValue(best_submission),
                SnakeCaseConstants.SubmissionsWithPoints ->
                    JsonUtils.toJsonValue(submissions_with_points.map(submission => submission.toJsValue()))
            )
        )
    }
}

object ExercisePointsData {
    def fromPointsSchema(
        exercisePointsSchema: PointsExerciseSchema,
        userId: Int,
        relatedCommitEventLinks: Seq[LinkTrait]
    ): ExercisePointsData = {
        ExercisePointsData(
            exercise_id = exercisePointsSchema.id,
            user_id = userId,
            submission_count = exercisePointsSchema.submission_count,
            commit_count = relatedCommitEventLinks.size,
            points = exercisePointsSchema.points,
            max_points = exercisePointsSchema.max_points,
            passed = exercisePointsSchema.passed,
            official = exercisePointsSchema.official,
            submissions = exercisePointsSchema.submissions,
            best_submission = exercisePointsSchema.best_submission,
            submissions_with_points = exercisePointsSchema.submissions_with_points.map(
                submission => ExerciseSubmissionData(
                    id = submission.id,
                    grade = submission.grade,
                    submission_time = submission.submission_time
                )
            )
        )
    }
}
