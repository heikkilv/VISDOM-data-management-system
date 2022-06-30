// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
