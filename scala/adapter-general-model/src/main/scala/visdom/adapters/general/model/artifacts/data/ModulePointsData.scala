package visdom.adapters.general.model.artifacts.data


import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.ModuleNumbersSchema
import visdom.adapters.general.schemas.PointsModuleSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ModulePointsData(
    module_id: Int,
    user_id: Int,
    exercise_count: Int,
    submission_count: Int,
    commit_count: Int,
    points: Int,
    max_points: Int,
    points_by_difficulty: PointsByDifficulty,
    passed: Boolean,
    cumulative_exercises: Int,
    cumulative_points: Int,
    cumulative_submissions: Int,
    cumulative_commits: Int
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.ModuleId -> JsonUtils.toBsonValue(module_id),
                SnakeCaseConstants.UserId -> JsonUtils.toBsonValue(user_id),
                SnakeCaseConstants.ExerciseCount -> JsonUtils.toBsonValue(exercise_count),
                SnakeCaseConstants.SubmissionCount -> JsonUtils.toBsonValue(submission_count),
                SnakeCaseConstants.CommitCount -> JsonUtils.toBsonValue(commit_count),
                SnakeCaseConstants.Points -> JsonUtils.toBsonValue(points),
                SnakeCaseConstants.MaxPoints -> JsonUtils.toBsonValue(max_points),
                SnakeCaseConstants.PointsByDifficulty -> points_by_difficulty.toBsonValue(),
                SnakeCaseConstants.Passed -> JsonUtils.toBsonValue(passed),
                SnakeCaseConstants.CumulativeExercises -> JsonUtils.toBsonValue(cumulative_exercises),
                SnakeCaseConstants.CumulativePoints -> JsonUtils.toBsonValue(cumulative_points),
                SnakeCaseConstants.CumulativeSubmissions -> JsonUtils.toBsonValue(cumulative_submissions),
                SnakeCaseConstants.CumulativeCommits -> JsonUtils.toBsonValue(cumulative_commits)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.ModuleId -> JsonUtils.toJsonValue(module_id),
                SnakeCaseConstants.UserId -> JsonUtils.toJsonValue(user_id),
                SnakeCaseConstants.ExerciseCount -> JsonUtils.toJsonValue(exercise_count),
                SnakeCaseConstants.SubmissionCount -> JsonUtils.toJsonValue(submission_count),
                SnakeCaseConstants.CommitCount -> JsonUtils.toJsonValue(commit_count),
                SnakeCaseConstants.Points -> JsonUtils.toJsonValue(points),
                SnakeCaseConstants.MaxPoints -> JsonUtils.toJsonValue(max_points),
                SnakeCaseConstants.PointsByDifficulty -> points_by_difficulty.toJsValue(),
                SnakeCaseConstants.Passed -> JsonUtils.toJsonValue(passed),
                SnakeCaseConstants.CumulativeExercises -> JsonUtils.toJsonValue(cumulative_exercises),
                SnakeCaseConstants.CumulativePoints -> JsonUtils.toJsonValue(cumulative_points),
                SnakeCaseConstants.CumulativeSubmissions -> JsonUtils.toJsonValue(cumulative_submissions),
                SnakeCaseConstants.CumulativeCommits -> JsonUtils.toJsonValue(cumulative_commits)
            )
        )
    }
}

object ModulePointsData {
    def fromPointsSchema(
        modulePointsSchema: PointsModuleSchema,
        userId: Int,
        exerciseCount: Int,
        commitCount: Int,
        cumulativeValues: ModuleNumbersSchema
    ): ModulePointsData = {
        ModulePointsData(
            module_id = modulePointsSchema.id,
            user_id = userId,
            exercise_count = exerciseCount,
            submission_count = modulePointsSchema.submission_count,
            commit_count = commitCount,
            points = modulePointsSchema.points,
            max_points = modulePointsSchema.max_points,
            points_by_difficulty = PointsByDifficulty(
                categoryN = modulePointsSchema.points_by_difficulty.category,
                categoryP = modulePointsSchema.points_by_difficulty.categoryP,
                categoryG = modulePointsSchema.points_by_difficulty.categoryG
            ),
            passed = modulePointsSchema.passed,
            cumulative_exercises = cumulativeValues.exercise_count,
            cumulative_points = cumulativeValues.point_count.total(),
            cumulative_submissions = cumulativeValues.submission_count,
            cumulative_commits = cumulativeValues.commit_count
        )
    }
}
