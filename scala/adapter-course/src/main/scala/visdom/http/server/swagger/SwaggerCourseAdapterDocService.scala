// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.swagger

import com.github.swagger.akka.model.Info
import visdom.adapters.course.AdapterValues
import visdom.http.server.services.CourseAdapterInfoService
import visdom.http.server.services.DataQueryService
import visdom.http.server.services.HistoryQueryService
import visdom.http.server.services.UsernameQueryService


object SwaggerCourseAdapterDocService extends SwaggerDocService {
    override val host: String = AdapterValues.apiAddress
    override val info: Info = Info(version = AdapterValues.Version)
    override val apiClasses: Set[Class[_]] = Set(
        classOf[CourseAdapterInfoService],
        classOf[DataQueryService],
        classOf[HistoryQueryService],
        classOf[UsernameQueryService]
    )
}
