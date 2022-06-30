// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
