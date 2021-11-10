package visdom.adapters.course.structs

import visdom.utils.CommonConstants
import visdom.utils.CourseUtils


final case class GradeDataCounts(
    students: Int,
    weeks: Map[String, ModuleDataCounts[Float]]
)

object GradeDataCounts {
    def weekString(weekNumber: Int): String = {
        weekNumber.toString.reverse.padTo(2, CommonConstants.ZeroChar).reverse
    }

    def getWeeks(data: Map[Int, GradeDataCounts]): Seq[String] = {
        val minWeek: Int = 1
        val maxWeek: Int = data
            .mapValues(dataCounts => dataCounts.weeks.keySet.toSeq)
            .map({case (_, weeks) => weeks.map(week => week.toInt)})
            .flatten
            .fold(minWeek)((week1, week2) => math.max(week1, week2))

        (minWeek to maxWeek).map(weekNumber => weekString(weekNumber))
    }

    def fillMissingData(data: Map[Int, GradeDataCounts]): Map[Int, GradeDataCounts] = {
        val weeks: Seq[String] = getWeeks(data)
        val emptyWeekData: ModuleDataCounts[Float] = ModuleDataCounts.getEmpty[Float]()

        (CourseUtils.MinGrade to CourseUtils.MaxGrade)
            .map(grade => (grade, data.get(grade)))
            .toMap
            .mapValues(
                gradeDataOption => gradeDataOption match {
                    case Some(gradeData: GradeDataCounts) => GradeDataCounts(
                        students = gradeData.students,
                        weeks = weeks
                            .map(week => (week, gradeData.weeks.get(week)))
                            .toMap
                            .mapValues(
                                weekDataOption => weekDataOption match {
                                    case Some(weekData: ModuleDataCounts[Float]) => weekData
                                    case None => emptyWeekData
                                }
                            )
                    )
                    case None => GradeDataCounts(
                        students = 0,
                        weeks = weeks.map(week => (week, emptyWeekData)).toMap
                    )
                }
            )
    }
}
