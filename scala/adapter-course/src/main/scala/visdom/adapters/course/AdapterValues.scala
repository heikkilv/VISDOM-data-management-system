package visdom.adapters.course

import akka.actor.Props
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import java.time.Instant
import scala.concurrent.ExecutionContextExecutor
import visdom.constants.ComponentConstants
import visdom.http.HttpConstants
import visdom.http.server.ServerConstants
import visdom.http.server.actors.CourseAdapterInfoActor
import visdom.http.server.actors.DataQueryActor
import visdom.http.server.services.CourseAdapterInfoService
import visdom.http.server.services.DataQueryService
import visdom.http.server.swagger.SwaggerCourseAdapterDocService
import visdom.http.server.swagger.SwaggerConstants
import visdom.http.server.swagger.SwaggerRoutes
import visdom.utils.CommonConstants
import visdom.utils.CourseAdapterEnvironmentVariables.CourseAdapterVariableMap
import visdom.utils.CourseAdapterEnvironmentVariables.EnvironmentAPlusDatabase
import visdom.utils.CourseAdapterEnvironmentVariables.EnvironmentGitlabDatabase
import visdom.utils.EnvironmentVariables.EnvironmentApplicationName
import visdom.utils.EnvironmentVariables.EnvironmentHostName
import visdom.utils.EnvironmentVariables.EnvironmentHostPort
import visdom.utils.EnvironmentVariables.getEnvironmentVariable


object AdapterValues {
    val startTime: String = Instant.now().toString()

    val componentName: String = getEnvironmentVariable(EnvironmentApplicationName, CourseAdapterVariableMap)
    val componentType: String = ComponentConstants.AdapterComponentType

    val hostServerName: String = getEnvironmentVariable(EnvironmentHostName, CourseAdapterVariableMap)
    val hostServerPort: String = getEnvironmentVariable(EnvironmentHostPort, CourseAdapterVariableMap)
    val apiAddress: String = List(hostServerName, hostServerPort).mkString(CommonConstants.DoubleDot)
    val swaggerDefinition: String = SwaggerConstants.SwaggerLocation

    val fullApiAddress: String =
        HttpConstants.HttpPrefix.concat(
            apiAddress.contains(HttpConstants.Localhost) match {
                case true => Seq(
                    getEnvironmentVariable(EnvironmentApplicationName),
                    ServerConstants.HttpInternalPort.toString()
                ).mkString(CommonConstants.DoubleDot)
                case false => apiAddress
            }
        )

    val aPlusDatabaseName: String = getEnvironmentVariable(EnvironmentAPlusDatabase, CourseAdapterVariableMap)
    val gitlabDatabaseName: String = getEnvironmentVariable(EnvironmentGitlabDatabase, CourseAdapterVariableMap)

    val AdapterType: String = ComponentConstants.CourseAdapterType
    val Version: String = "0.1"

    implicit val system: ActorSystem = ActorSystem(ServerConstants.DefaultActorSystem)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val routes = Directives.concat(
        new CourseAdapterInfoService(system.actorOf(Props[CourseAdapterInfoActor])).route,
        new DataQueryService(system.actorOf(Props[DataQueryActor])).route,
        SwaggerRoutes.getSwaggerRouter(SwaggerCourseAdapterDocService)
    )
}
