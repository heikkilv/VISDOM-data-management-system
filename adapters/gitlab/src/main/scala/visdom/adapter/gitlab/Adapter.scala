package visdom.adapter.gitlab

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Terminated
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.concat
import java.time.Instant
import org.apache.spark.sql.SparkSession
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.sys.ShutdownHookThread
import visdom.adapter.gitlab.queries.commits.CommitDataService
import visdom.adapter.gitlab.queries.commits.CommitDataActor
import visdom.adapter.gitlab.queries.info.InfoActor
import visdom.adapter.gitlab.queries.info.InfoService
import visdom.adapter.gitlab.queries.swagger.SwaggerDocService
import visdom.adapter.gitlab.queries.swagger.SwaggerUiSite
import visdom.adapter.gitlab.queries.timestamps.TimestampService
import visdom.adapter.gitlab.queries.timestamps.TimestampActor
import visdom.spark.Session


object Adapter extends App with SwaggerUiSite {
    final val StartTime: String = Instant.now().toString()
    final val AdapterName: String = sys.env.getOrElse(
        GitlabConstants.EnvironmentApplicationName,
        GitlabConstants.DefaultApplicationName
    )
    final val ApiAddress: String = List(
        sys.env.getOrElse(GitlabConstants.EnvironmentHostName, GitlabConstants.DefaultHostName),
        sys.env.getOrElse(GitlabConstants.EnvironmentHostPort, GitlabConstants.DefaultHostPort)
    ).mkString(GitlabConstants.DoubleDot)

    val sparkSession: SparkSession = Session.sparkSession

    implicit val system: ActorSystem = ActorSystem("akka-http-sample")
    val shutDownHookThread: ShutdownHookThread = sys.addShutdownHook({
        val termination: Future[Terminated] = {
            Session.sparkSession.stop()
            system.terminate()
        }
    })
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val routes = concat(
        new CommitDataService(system.actorOf(Props[CommitDataActor])).route,
        new TimestampService(system.actorOf(Props[TimestampActor])).route,
        new InfoService(system.actorOf(Props[InfoActor])).route,
        SwaggerDocService.routes,
        swaggerUiSiteRoute
    )

    val serverBinding: Future[Http.ServerBinding] =
        Http().newServerAt("0.0.0.0", GitlabConstants.HttpInternalPort).bindFlow(routes)
}
