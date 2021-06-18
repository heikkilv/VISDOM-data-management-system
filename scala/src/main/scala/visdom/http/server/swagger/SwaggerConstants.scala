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
