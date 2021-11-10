package visdom.adapters.course.structs

import visdom.utils.CommonConstants
import visdom.utils.CourseUtils


final case class GradeCumulativeCounts(
    students: Int,
    weeks: Map[String, ModuleDataCountsWithCumulative[Float]]
)

object GradeCumulativeCounts {
    def weekString(weekNumber: Int): String = {
        weekNumber.toString.reverse.padTo(2, CommonConstants.ZeroChar).reverse
    }

    def getEmptyGradeData(week: Int): GradeCumulativeCounts = {
        GradeCumulativeCounts(
            students = 0,
            weeks = Map(
                weekString(week) -> ModuleDataCountsWithCumulative.getEmptyFloats(None)
            )
        )
    }

    def getNewGradeData(week: Int, gradeData: GradeCumulativeCounts): GradeCumulativeCounts = {
        GradeCumulativeCounts(
            students = gradeData.students,
            weeks = gradeData.weeks ++ Map(
                weekString(week) -> ModuleDataCountsWithCumulative.getEmptyFloats(
                    gradeData.weeks.get(weekString(week - 1))
                )
            )
        )
    }

    def fillMissingData(data: Map[Int, GradeCumulativeCounts]): Map[Int, GradeCumulativeCounts] = {
        val minWeek: Int = 1
        val maxWeek =
            data
                .mapValues(dataCounts => dataCounts.weeks.keySet.toSeq)
                .map({case (_, weeks) => weeks.map(week => week.toInt)})
                .flatten
                .fold(minWeek)((week1, week2) => math.max(week1, week2))

        def fillData(grade: Int, week: Int, fullData: Map[Int, GradeCumulativeCounts]): Map[Int, GradeCumulativeCounts] = {
            grade > CourseUtils.MaxGrade match {
                // all grade-week pairs have been gone through => return the filled data
                case true => fullData
                case false => week > maxWeek match {
                    // all weeks have been gone through for the current grade => move to the next grade
                    case true => fillData(grade + 1, minWeek, fullData)
                    case false => fullData.get(grade) match {
                        case Some(gradeData: GradeCumulativeCounts) => gradeData.weeks.get(weekString(week)) match {
                            // data for the grade-week pair already exists => move to the next pair
                            case Some(_) => fillData(grade, week + 1, fullData)
                            // no data for the current week => add zeroes and calculate cumulative values
                            case None => fillData(
                                grade,
                                week + 1,
                                fullData ++ Map(grade -> getNewGradeData(week, gradeData))
                            )
                        }
                        // no data for the current grade => add the grade to the data with zeroes for the current week
                        case None => fillData(grade, week + 1, fullData ++ Map(grade -> getEmptyGradeData(week)))
                    }
                }
            }
        }

        fillData(CourseUtils.MinGrade, minWeek, data)
    }
}
