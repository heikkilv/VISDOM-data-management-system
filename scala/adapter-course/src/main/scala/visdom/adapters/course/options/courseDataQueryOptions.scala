// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.course.options

import visdom.http.server.QueryOptionsBase
import visdom.utils.GeneralUtils


final case class CourseDataQueryOptions(
    courseId: Int,
    username: Option[String],
    exerciseId: Option[Int],
    includeFuture: Boolean
)
extends BaseQueryOptions

final case class CourseDataQueryInput(
    courseId: String,
    username: Option[String],
    exerciseId: Option[String],
    includeFuture: String
)
extends QueryOptionsBase
{
    def toCourseDataQueryOptions(): Option[CourseDataQueryOptions] = {
        (
            GeneralUtils.isIdNumber(courseId) &&
            GeneralUtils.isIdNumber(exerciseId) &&
            GeneralUtils.isBooleanString(includeFuture)
        ) match {
            case true => Some(
                CourseDataQueryOptions(
                    courseId = courseId.toInt,
                    username = username,
                    exerciseId = exerciseId.map(id => id.toInt),
                    includeFuture = includeFuture.toBoolean
                )
            )
            case false => None
        }
    }
}
