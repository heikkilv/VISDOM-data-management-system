package visdom.broker

import akka.http.scaladsl.server.Directives
import java.time.Instant
import visdom.http.server.swagger.SwaggerRoutes
import visdom.utils.CommonConstants
import visdom.utils.EnvironmentVariables.EnvironmentApplicationName
import visdom.utils.EnvironmentVariables.EnvironmentHostName
import visdom.utils.EnvironmentVariables.EnvironmentHostPort
import visdom.utils.EnvironmentVariables.getEnvironmentVariable


object BrokerValues {
    val startTime: String = Instant.now().toString()

    val componentName: String = getEnvironmentVariable(EnvironmentApplicationName)
    val componentType: String = "broker"

    val hostServerName: String = getEnvironmentVariable(EnvironmentHostName)
    val hostServerPort: String = getEnvironmentVariable(EnvironmentHostPort)
    val apiAddress: String = List(hostServerName, hostServerPort).mkString(CommonConstants.DoubleDot)

    val brokerVersion: String = "0.1"

    val routes = Directives.concat(
        SwaggerRoutes.getSwaggerRouter(SwaggerBrokerDocService)
    )
}
