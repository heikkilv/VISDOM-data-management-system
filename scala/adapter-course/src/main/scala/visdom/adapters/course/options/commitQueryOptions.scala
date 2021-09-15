package visdom.adapters.course.options

import visdom.http.server.QueryOptionsBase
import visdom.utils.GeneralUtils


final case class CommitQueryOptions(
    fullName: String,
    courseId: Int,
    exerciseId: Int
)

final case class CommitQueryInput(
    fullName: String,
    courseId: String,
    exerciseId: String
)
extends QueryOptionsBase
{
    def toCommitQueryOptions(): Option[CommitQueryOptions] = {
        (GeneralUtils.isIdNumber(courseId) && GeneralUtils.isIdNumber(exerciseId)) match {
            case true => Some(
                CommitQueryOptions(
                    fullName = fullName,
                    courseId = courseId.toInt,
                    exerciseId = exerciseId.toInt
                )
            )
            case false => None
        }
    }
}
