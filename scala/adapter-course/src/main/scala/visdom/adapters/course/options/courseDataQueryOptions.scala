package visdom.adapters.course.options

import visdom.http.server.QueryOptionsBase
import visdom.utils.GeneralUtils


final case class CourseDataQueryOptions(
    courseId: Int,
    fullName: Option[String],
    exerciseId: Option[Int]
)

final case class CourseDataQueryInput(
    courseId: String,
    fullName: Option[String],
    exerciseId: Option[String]
)
extends QueryOptionsBase
{
    def toCourseDataQueryOptions(): Option[CourseDataQueryOptions] = {
        (GeneralUtils.isIdNumber(courseId) && GeneralUtils.isIdNumber(exerciseId)) match {
            case true => Some(
                CourseDataQueryOptions(
                    courseId = courseId.toInt,
                    fullName = fullName,
                    exerciseId = exerciseId.map(id => id.toInt)
                )
            )
            case false => None
        }
    }
}
