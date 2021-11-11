package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.adapters.course.structs.GradeCumulativeDataCounts
import visdom.adapters.course.structs.ModuleCumulativeDataCounts


final case class FullHistoryOutput(
    data_by_grades: Map[String, HistoryWeekDataForGrade],
    data_by_weeks: Map[String, HistoryGradeDataForWeek]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.DataByGrades -> JsonUtils.toJsonValue(data_by_grades),
            SnakeCaseConstants.DataByWeeks -> JsonUtils.toJsonValue(data_by_weeks)
        )
    }
}

object FullHistoryOutput {
    def getDataByGrades(gradeWeekMapData: Map[Int, GradeCumulativeDataCounts]): Map[String, HistoryWeekDataForGrade] = {
        gradeWeekMapData.map({
            case (grade, gradeData) => {
                val sortedWeekData: Seq[ModuleCumulativeDataCounts[Float]] =
                    gradeData
                        .weeks
                        .toSeq
                        .sortBy({case (week, _) => week})
                        .map({case (_, data) => data})

                (
                    grade.toString,
                    HistoryWeekDataForGrade(
                        student_count = gradeData.students,
                        avg_points = sortedWeekData.map(data => data.points),
                        avg_exercises = sortedWeekData.map(data => data.exercises),
                        avg_submissions = sortedWeekData.map(data => data.submissions),
                        avg_commits = sortedWeekData.map(data => data.commits),
                        avg_cum_points = sortedWeekData.map(data => data.cumulativePoints),
                        avg_cum_exercises = sortedWeekData.map(data => data.cumulativeExercises),
                        avg_cum_submissions = sortedWeekData.map(data => data.cumulativeSubmissions),
                        avg_cum_commits = sortedWeekData.map(data => data.cumulativePoints)
                    )
                )
            }
        })
    }

    def getDataByWeeks(gradeWeekMapData: Map[Int, GradeCumulativeDataCounts]): Map[String, HistoryGradeDataForWeek] = {
        gradeWeekMapData
            .map({
                case (grade, gradeData) =>
                    gradeData.weeks.map({
                        case (week, weekData) => ((grade, week), (gradeData.students, weekData))
                    })
            })
            .flatten
            .groupBy({case ((_, week), _) => week})
            .mapValues(
                gradeData =>
                    gradeData
                        .map({
                            case ((grade, _), (studentCount, data)) => (
                                grade.toString,
                                (studentCount, data)
                            )
                        })
                        .toSeq
                        .sortBy({case (grade, _) => grade})
                        .map({case (_, data) => data})
            )
            .mapValues(
                data => {
                    val counts: Seq[ModuleCumulativeDataCounts[Float]] =
                        data.map({case (_, countData) => countData})

                    HistoryGradeDataForWeek(
                        student_counts = data.map({case (studentCounts, _) => studentCounts}),
                        avg_points = counts.map(values => values.points),
                        avg_exercises = counts.map(values => values.exercises),
                        avg_submissions = counts.map(values => values.submissions),
                        avg_commits = counts.map(values => values.commits),
                        avg_cum_points = counts.map(values => values.cumulativePoints),
                        avg_cum_exercises = counts.map(values => values.cumulativeExercises),
                        avg_cum_submissions = counts.map(values => values.cumulativeSubmissions),
                        avg_cum_commits = counts.map(values => values.cumulativeCommits)
                    )
                }
            )
    }

    def fromGradeWeekData(gradeWeekMapData: Map[Int, GradeCumulativeDataCounts]): FullHistoryOutput = {
        FullHistoryOutput(
            data_by_grades = getDataByGrades(gradeWeekMapData),
            data_by_weeks = getDataByWeeks(gradeWeekMapData)
        )
    }
}
