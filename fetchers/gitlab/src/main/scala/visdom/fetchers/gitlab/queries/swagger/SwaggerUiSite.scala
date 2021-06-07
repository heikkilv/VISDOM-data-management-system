package visdom.fetchers.gitlab

import akka.http.scaladsl.server.Directives
import visdom.fetchers.gitlab.queries.swagger.SwaggerConstants


trait SwaggerUiSite extends Directives {
    val swaggerUiSiteRoute = concat(
        path(SwaggerConstants.SwaggerUiPath) {
            getFromResource(SwaggerConstants.SwaggerUiResourceFile)
        },
        getFromResourceDirectory(SwaggerConstants.SwaggerUiResourceFolder)
    )
}
