package visdom.fetchers.aplus

import akka.actor.Props
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import java.time.Instant
import org.mongodb.scala.MongoDatabase
import scala.concurrent.ExecutionContextExecutor
import visdom.constants.ComponentConstants
import visdom.database.mongodb.MongoConnection
import visdom.http.server.ServerConstants
import visdom.http.server.actors.APlusInfoActor
import visdom.http.server.actors.CourseActor
import visdom.http.server.actors.ModuleActor
import visdom.http.server.services.APlusInfoService
import visdom.http.server.services.CourseService
import visdom.http.server.services.ModuleService
import visdom.http.server.swagger.SwaggerAPlusFetcherDocService
import visdom.http.server.swagger.SwaggerConstants
import visdom.http.server.swagger.SwaggerRoutes
import visdom.utils.APlusEnvironmentVariables.APlusVariableMap
import visdom.utils.APlusEnvironmentVariables.EnvironmentAPlusHost
import visdom.utils.APlusEnvironmentVariables.EnvironmentAPlusInsecureConnection
import visdom.utils.APlusEnvironmentVariables.EnvironmentAPlusToken
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
    val sourceServerToken: String = getEnvironmentVariable(EnvironmentAPlusToken, APlusVariableMap)
    val sourceServerInsecureConnection: Boolean =
        getEnvironmentVariable(EnvironmentAPlusInsecureConnection, APlusVariableMap).toBoolean

    val targetDatabaseName: String = getEnvironmentVariable(EnvironmentDataDatabase, APlusVariableMap)
    val targetDatabase: MongoDatabase = MongoConnection.mongoClient.getDatabase(targetDatabaseName)

    val FetcherType: String = ComponentConstants.APlusFetcherType
    val FetcherVersion: String = "0.1"

    val targetServer: APlusServer = new APlusServer(
        hostAddress = sourceServer,
        apiToken = Some(sourceServerToken),
        allowUnsafeSSL = Some(sourceServerInsecureConnection)
    )

    implicit val system: ActorSystem = ActorSystem(ServerConstants.DefaultActorSystem)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val routes = Directives.concat(
        new CourseService(system.actorOf(Props[CourseActor])).route,
        new ModuleService(system.actorOf(Props[ModuleActor])).route,
        new APlusInfoService(system.actorOf(Props[APlusInfoActor])).route,
        SwaggerRoutes.getSwaggerRouter(SwaggerAPlusFetcherDocService)
    )
}
