package visdom.adapter.gitlab.queries.swagger


object SwaggerConstants {
    final val SwaggerJsonVersion: String = "0.2"
    final val SwaggerJsonUnwantedDefinitions: Seq[String] = Seq(
        "Function1",
        "Function1RequestContextFutureRouteResult"
    )

    final val SwaggerUiPath: String = "swagger"
    final val SwaggerUiResourceFile: String = "swagger-ui/index.html"
    final val SwaggerUiResourceFolder: String = "swagger-ui"
}
