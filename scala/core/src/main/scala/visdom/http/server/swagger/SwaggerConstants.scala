// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.swagger


object SwaggerConstants {
    final val SwaggerJsonVersionDefault: String = "0.1"
    final val SwaggerJsonUnwantedDefinitions: Seq[String] = Seq(
        "Function1",
        "Function1RequestContextFutureRouteResult",
        "MapStringJsValue"
    )

    final val SwaggerUiPath: String = "swagger"
    final val SwaggerUiResourceFile: String = "swagger-ui/index.html"
    final val SwaggerUiResourceFolder: String = "swagger-ui"

    final val SwaggerLocation: String = "/api-docs/swagger.json"
}
