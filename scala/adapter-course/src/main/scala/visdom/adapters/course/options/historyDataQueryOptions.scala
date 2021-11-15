package visdom.adapters.course.options

import visdom.http.server.QueryOptionsBase
import visdom.utils.GeneralUtils


final case class HistoryDataQueryOptions(
    courseId: Int
)
extends BaseQueryOptions

final case class HistoryDataQueryInput(
    courseId: String
)
extends QueryOptionsBase
{
    def toUsernameQueryOptions(): Option[HistoryDataQueryOptions] = {
        GeneralUtils.isIdNumber(courseId) match {
            case true => Some(
                HistoryDataQueryOptions(
                    courseId = courseId.toInt
                )
            )
            case false => None
        }
    }
}
