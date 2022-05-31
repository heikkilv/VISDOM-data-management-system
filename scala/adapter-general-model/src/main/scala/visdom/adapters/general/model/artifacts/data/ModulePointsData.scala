package visdom.adapters.general.model.artifacts.data


import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.model.metadata.data.ModuleData
import visdom.adapters.general.schemas.ModuleNumbersSchema
import visdom.adapters.general.schemas.PointsModuleSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ModulePointsData(
    module_id: Int,
    module_number: Int,
    user_id: Int,
    exercise_count: Int,
    submission_count: Int,
    commit_count: Int,
    points: Int,
    max_points: Int,
    max_exercise_count: Int,
    points_by_difficulty: PointsByDifficulty,
    passed: Boolean,
    cumulative_exercise_count: Int,
    cumulative_max_exercise_count: Int,
    cumulative_points: Int,
    cumulative_max_points: Int,
    cumulative_submission_count: Int,
    cumulative_commit_count: Int
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.ModuleId -> JsonUtils.toBsonValue(module_id),
                SnakeCaseConstants.ModuleNumber -> JsonUtils.toBsonValue(module_number),
                SnakeCaseConstants.UserId -> JsonUtils.toBsonValue(user_id),
                SnakeCaseConstants.ExerciseCount -> JsonUtils.toBsonValue(exercise_count),
                SnakeCaseConstants.SubmissionCount -> JsonUtils.toBsonValue(submission_count),
                SnakeCaseConstants.CommitCount -> JsonUtils.toBsonValue(commit_count),
                SnakeCaseConstants.Points -> JsonUtils.toBsonValue(points),
                SnakeCaseConstants.MaxPoints -> JsonUtils.toBsonValue(max_points),
                SnakeCaseConstants.MaxExerciseCount -> JsonUtils.toBsonValue(max_exercise_count),
                SnakeCaseConstants.PointsByDifficulty -> points_by_difficulty.toBsonValue(),
                SnakeCaseConstants.Passed -> JsonUtils.toBsonValue(passed),
                SnakeCaseConstants.CumulativeExerciseCount -> JsonUtils.toBsonValue(cumulative_exercise_count),
                SnakeCaseConstants.CumulativeMaxExerciseCount -> JsonUtils.toBsonValue(cumulative_max_exercise_count),
                SnakeCaseConstants.CumulativePoints -> JsonUtils.toBsonValue(cumulative_points),
                SnakeCaseConstants.CumulativeMaxPoints -> JsonUtils.toBsonValue(cumulative_max_points),
                SnakeCaseConstants.CumulativeSubmissionCount -> JsonUtils.toBsonValue(cumulative_submission_count),
                SnakeCaseConstants.CumulativeCommitCount -> JsonUtils.toBsonValue(cumulative_commit_count)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.ModuleId -> JsonUtils.toJsonValue(module_id),
                SnakeCaseConstants.ModuleNumber -> JsonUtils.toJsonValue(module_number),
                SnakeCaseConstants.UserId -> JsonUtils.toJsonValue(user_id),
                SnakeCaseConstants.ExerciseCount -> JsonUtils.toJsonValue(exercise_count),
                SnakeCaseConstants.SubmissionCount -> JsonUtils.toJsonValue(submission_count),
                SnakeCaseConstants.CommitCount -> JsonUtils.toJsonValue(commit_count),
                SnakeCaseConstants.Points -> JsonUtils.toJsonValue(points),
                SnakeCaseConstants.MaxPoints -> JsonUtils.toJsonValue(max_points),
                SnakeCaseConstants.MaxExerciseCount -> JsonUtils.toJsonValue(max_exercise_count),
                SnakeCaseConstants.PointsByDifficulty -> points_by_difficulty.toJsValue(),
                SnakeCaseConstants.Passed -> JsonUtils.toJsonValue(passed),
                SnakeCaseConstants.CumulativeExerciseCount -> JsonUtils.toJsonValue(cumulative_exercise_count),
                SnakeCaseConstants.CumulativeMaxExerciseCount -> JsonUtils.toJsonValue(cumulative_max_exercise_count),
                SnakeCaseConstants.CumulativePoints -> JsonUtils.toJsonValue(cumulative_points),
                SnakeCaseConstants.CumulativeMaxPoints -> JsonUtils.toJsonValue(cumulative_max_points),
                SnakeCaseConstants.CumulativeSubmissionCount -> JsonUtils.toJsonValue(cumulative_submission_count),
                SnakeCaseConstants.CumulativeCommitCount -> JsonUtils.toJsonValue(cumulative_commit_count)
            )
        )
    }
}

object ModulePointsData {
    def fromPointsSchema(
        modulePointsSchema: PointsModuleSchema,
        userId: Int,
        exerciseCount: Int,
        maxExerciseCount: Int,
        commitCount: Int,
        cumulativeValues: ModuleNumbersSchema,
        cumulativeMaxPoints: Int,
        cumulativeMaxExerciseCount: Int
    ): ModulePointsData = {
        ModulePointsData(
            module_id = modulePointsSchema.id,
            module_number = ModuleData.getModuleNumber(modulePointsSchema.name),
            user_id = userId,
            exercise_count = exerciseCount,
            submission_count = modulePointsSchema.submission_count,
            commit_count = commitCount,
            points = modulePointsSchema.points,
            max_points = modulePointsSchema.max_points,
            max_exercise_count = maxExerciseCount,
            points_by_difficulty = PointsByDifficulty(
                categoryN = modulePointsSchema.points_by_difficulty.category,
                categoryP = modulePointsSchema.points_by_difficulty.categoryP,
                categoryG = modulePointsSchema.points_by_difficulty.categoryG
            ),
            passed = modulePointsSchema.passed,
            cumulative_exercise_count = cumulativeValues.exercise_count,
            cumulative_points = cumulativeValues.point_count.total(),
            cumulative_submission_count = cumulativeValues.submission_count,
            cumulative_commit_count = cumulativeValues.commit_count,
            cumulative_max_points = cumulativeMaxPoints,
            cumulative_max_exercise_count = cumulativeMaxExerciseCount
        )
    }
}
