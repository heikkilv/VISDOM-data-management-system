// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.swagger

import akka.http.scaladsl.server.Directives


trait SwaggerUiSite extends Directives {
    val swaggerUiSiteRoute = concat(
        path(SwaggerConstants.SwaggerUiPath) {
            getFromResource(SwaggerConstants.SwaggerUiResourceFile)
        },
        getFromResourceDirectory(SwaggerConstants.SwaggerUiResourceFolder)
    )
}
