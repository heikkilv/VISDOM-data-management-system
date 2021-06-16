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
