package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


abstract class HistoryData(
    avg_points: Seq[Float],
    avg_exercises: Seq[Float],
    avg_submissions: Seq[Float],
    avg_commits: Seq[Float],
    avg_cum_points: Seq[Float],
    avg_cum_exercises: Seq[Float],
    avg_cum_submissions: Seq[Float],
    avg_cum_commits: Seq[Float]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.AvgPoints -> JsonUtils.toJsonValue(avg_points),
            SnakeCaseConstants.AvgExercises -> JsonUtils.toJsonValue(avg_exercises),
            SnakeCaseConstants.AvgSubmissions -> JsonUtils.toJsonValue(avg_submissions),
            SnakeCaseConstants.AvgCommits -> JsonUtils.toJsonValue(avg_commits),
            SnakeCaseConstants.AvgCumPoints -> JsonUtils.toJsonValue(avg_cum_points),
            SnakeCaseConstants.AvgCumExercises -> JsonUtils.toJsonValue(avg_cum_exercises),
            SnakeCaseConstants.AvgCumSubmissions -> JsonUtils.toJsonValue(avg_cum_submissions),
            SnakeCaseConstants.AvgCumCommits -> JsonUtils.toJsonValue(avg_cum_commits)
        )
    }
}
