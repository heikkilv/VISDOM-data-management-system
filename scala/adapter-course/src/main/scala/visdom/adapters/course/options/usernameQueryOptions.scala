package visdom.adapters.course.options

import visdom.http.server.QueryOptionsBase
import visdom.utils.GeneralUtils


final case class UsernameQueryOptions(
    courseId: Int
)
extends BaseQueryOptions

final case class UsernameQueryInput(
    courseId: String
)
extends QueryOptionsBase
{
    def toUsernameQueryOptions(): Option[UsernameQueryOptions] = {
        GeneralUtils.isIdNumber(courseId) match {
            case true => Some(
                UsernameQueryOptions(
                    courseId = courseId.toInt
                )
            )
            case false => None
        }
    }
}
