package visdom.fetchers.aplus

import akka.actor.Props
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import java.time.Instant
import scala.concurrent.ExecutionContextExecutor
import visdom.constants.ComponentConstants
import visdom.http.server.ServerConstants
import visdom.http.server.actors.APlusFetcherInfoActor
import visdom.http.server.services.APlusFetcherInfoService
import visdom.http.server.swagger.SwaggerAPlusFetcherDocService
import visdom.http.server.swagger.SwaggerConstants
import visdom.http.server.swagger.SwaggerRoutes
import visdom.utils.APlusEnvironmentVariables.APlusVariableMap
import visdom.utils.APlusEnvironmentVariables.EnvironmentAPlusHost
import visdom.utils.APlusEnvironmentVariables.EnvironmentDataDatabase
import visdom.utils.CommonConstants
import visdom.utils.EnvironmentVariables.EnvironmentApplicationName
import visdom.utils.EnvironmentVariables.EnvironmentHostName
import visdom.utils.EnvironmentVariables.EnvironmentHostPort
import visdom.utils.EnvironmentVariables.getEnvironmentVariable


object FetcherValues {
    val startTime: String = Instant.now().toString()

    val componentName: String = getEnvironmentVariable(EnvironmentApplicationName, APlusVariableMap)
    val componentType: String = ComponentConstants.FetcherComponentType

    val hostServerName: String = getEnvironmentVariable(EnvironmentHostName, APlusVariableMap)
    val hostServerPort: String = getEnvironmentVariable(EnvironmentHostPort, APlusVariableMap)
    val apiAddress: String = List(hostServerName, hostServerPort).mkString(CommonConstants.DoubleDot)
    val swaggerDefinition: String = SwaggerConstants.SwaggerLocation

    val sourceServer: String = getEnvironmentVariable(EnvironmentAPlusHost, APlusVariableMap)
    val mongoDatabase: String = getEnvironmentVariable(EnvironmentDataDatabase, APlusVariableMap)

    val FetcherType: String = ComponentConstants.APlusFetcherType
    val FetcherVersion: String = "0.1"

    implicit val system: ActorSystem = ActorSystem(ServerConstants.DefaultActorSystem)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val routes = Directives.concat(
        new APlusFetcherInfoService(system.actorOf(Props[APlusFetcherInfoActor])).route,
        SwaggerRoutes.getSwaggerRouter(SwaggerAPlusFetcherDocService)
    )
}
