// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.swagger

import com.github.swagger.akka.model.Info
import visdom.fetchers.aplus.FetcherValues
import visdom.http.server.services.APlusInfoService
import visdom.http.server.services.CourseService
import visdom.http.server.services.ExerciseService
import visdom.http.server.services.ModuleService


object SwaggerAPlusFetcherDocService extends SwaggerDocService {
    override val host = FetcherValues.apiAddress
    override val info: Info = Info(version = FetcherValues.FetcherVersion)
    override val apiClasses: Set[Class[_]] = Set(
        classOf[CourseService],
        classOf[ModuleService],
        classOf[ExerciseService],
        classOf[APlusInfoService]
    )
}
