package visdom.broker

import akka.actor.Props
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import java.time.Instant
import scala.concurrent.ExecutionContextExecutor
import visdom.http.server.ServerConstants
import visdom.http.server.services.BrokerInfoService
import visdom.http.server.swagger.SwaggerConstants
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
    val swaggerDefinition: String = SwaggerConstants.SwaggerLocation

    val brokerVersion: String = "0.1"

    implicit val system: ActorSystem = ActorSystem(ServerConstants.DefaultActorSystem)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val routes = Directives.concat(
        new BrokerInfoService(system.actorOf(Props[BrokerInfoActor])).route,
        SwaggerRoutes.getSwaggerRouter(SwaggerBrokerDocService)
    )
}
