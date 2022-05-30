package visdom.adapters.general.model.artifacts.data


import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.ModuleAverageSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ModuleAverageData(
    module_number: Int,
    course_id: Int,
    grade: Int,
    total: Int,
    avg_points: Double,
    avg_exercises: Double,
    avg_submissions: Double,
    avg_commits: Double,
    avg_cum_points: Double,
    avg_cum_exercises: Double,
    avg_cum_submissions: Double,
    avg_cum_commits: Double
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.ModuleNumber -> JsonUtils.toBsonValue(module_number),
                SnakeCaseConstants.CourseId -> JsonUtils.toBsonValue(course_id),
                SnakeCaseConstants.Grade -> JsonUtils.toBsonValue(grade),
                SnakeCaseConstants.Total -> JsonUtils.toBsonValue(total),
                SnakeCaseConstants.AvgPoints -> JsonUtils.toBsonValue(avg_points),
                SnakeCaseConstants.AvgExercises -> JsonUtils.toBsonValue(avg_exercises),
                SnakeCaseConstants.AvgSubmissions -> JsonUtils.toBsonValue(avg_submissions),
                SnakeCaseConstants.AvgCommits -> JsonUtils.toBsonValue(avg_commits),
                SnakeCaseConstants.AvgCumPoints -> JsonUtils.toBsonValue(avg_cum_points),
                SnakeCaseConstants.AvgCumExercises -> JsonUtils.toBsonValue(avg_cum_exercises),
                SnakeCaseConstants.AvgCumSubmissions -> JsonUtils.toBsonValue(avg_cum_submissions),
                SnakeCaseConstants.AvgCumCommits -> JsonUtils.toBsonValue(avg_cum_commits)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.ModuleNumber -> JsonUtils.toJsonValue(module_number),
                SnakeCaseConstants.CourseId -> JsonUtils.toJsonValue(course_id),
                SnakeCaseConstants.Grade -> JsonUtils.toJsonValue(grade),
                SnakeCaseConstants.Total -> JsonUtils.toJsonValue(total),
                SnakeCaseConstants.AvgPoints -> JsonUtils.toJsonValue(avg_points),
                SnakeCaseConstants.AvgExercises -> JsonUtils.toJsonValue(avg_exercises),
                SnakeCaseConstants.AvgSubmissions -> JsonUtils.toJsonValue(avg_submissions),
                SnakeCaseConstants.AvgCommits -> JsonUtils.toJsonValue(avg_commits),
                SnakeCaseConstants.AvgCumPoints -> JsonUtils.toJsonValue(avg_cum_points),
                SnakeCaseConstants.AvgCumExercises -> JsonUtils.toJsonValue(avg_cum_exercises),
                SnakeCaseConstants.AvgCumSubmissions -> JsonUtils.toJsonValue(avg_cum_submissions),
                SnakeCaseConstants.AvgCumCommits -> JsonUtils.toJsonValue(avg_cum_commits)
            )
        )
    }
}

object ModuleAverageData {
    def fromModuleAverageSchema(
        moduleAverageSchema: ModuleAverageSchema,
        cumulativeValues: ModuleAverageSchema,
        courseId: Int
    ): ModuleAverageData = {
        ModuleAverageData(
            module_number = moduleAverageSchema.module_number,
            course_id = courseId,
            grade = moduleAverageSchema.grade,
            total = moduleAverageSchema.total,
            avg_points = moduleAverageSchema.avg_points,
            avg_exercises = moduleAverageSchema.avg_exercises,
            avg_submissions = moduleAverageSchema.avg_submissions,
            avg_commits = moduleAverageSchema.avg_commits,
            avg_cum_points = cumulativeValues.avg_points,
            avg_cum_exercises = cumulativeValues.avg_exercises,
            avg_cum_submissions = cumulativeValues.avg_submissions,
            avg_cum_commits = cumulativeValues.avg_commits
        )
    }
}
