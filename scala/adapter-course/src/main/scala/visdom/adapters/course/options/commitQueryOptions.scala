package visdom.adapters.course.options

import visdom.http.server.QueryOptionsBase
import visdom.utils.GeneralUtils


final case class CommitQueryOptions(
    fullName: String,
    courseId: Int,
    exerciseId: Option[Int]
)

final case class CommitQueryInput(
    fullName: String,
    courseId: String,
    exerciseId: Option[String]
)
extends QueryOptionsBase
{
    def toCommitQueryOptions(): Option[CommitQueryOptions] = {
        (GeneralUtils.isIdNumber(courseId) && GeneralUtils.isIdNumber(exerciseId)) match {
            case true => Some(
                CommitQueryOptions(
                    fullName = fullName,
                    courseId = courseId.toInt,
                    exerciseId = exerciseId.map(id => id.toInt)
                )
            )
            case false => None
        }
    }
}
