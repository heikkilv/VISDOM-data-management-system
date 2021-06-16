package visdom.adapter.gitlab.queries.swagger

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import org.apache.commons.lang.StringUtils.EMPTY


object SwaggerConstants {
    final val SwaggerJsonVersion: String = "0.2"
    final val SwaggerJsonUnwantedDefinitions: Seq[String] = Seq(
        "Function1",
        "Function1RequestContextFutureRouteResult",
        "MapStringJsValue"
    )

    final val SwaggerUiPath: String = "swagger"
    final val SwaggerUiResourceFile: String = "swagger-ui/index.html"
    final val SwaggerUiResourceFolder: String = "swagger-ui"

    val RootToSwaggerRedirect: Route = Directives.path(EMPTY) {
        Directives.get {
            Directives.redirect(SwaggerUiPath, StatusCodes.PermanentRedirect)
        }
    }
}
