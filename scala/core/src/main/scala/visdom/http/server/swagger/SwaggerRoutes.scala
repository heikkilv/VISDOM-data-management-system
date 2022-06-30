// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.swagger

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import org.apache.commons.lang.StringUtils.EMPTY


object SwaggerRoutes extends SwaggerUiSite {
    val rootToSwaggerRedirect: Route = Directives.path(EMPTY) {
        Directives.get {
            Directives.redirect(SwaggerConstants.SwaggerUiPath, StatusCodes.PermanentRedirect)
        }
    }

    def getSwaggerRouter(swaggerDocService: SwaggerDocService): Route = {
        Directives.concat(
            swaggerDocService.routes,
            swaggerUiSiteRoute,
            rootToSwaggerRedirect
        )
    }
}
