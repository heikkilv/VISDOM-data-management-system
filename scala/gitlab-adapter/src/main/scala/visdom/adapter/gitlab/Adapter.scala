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
import visdom.adapter.gitlab.queries.projects.ProjectDataActor
import visdom.adapter.gitlab.queries.projects.ProjectDataService
import visdom.adapter.gitlab.queries.timestamps.TimestampService
import visdom.adapter.gitlab.queries.timestamps.TimestampActor
import visdom.http.server.swagger.SwaggerRoutes
import visdom.spark.Session


object Adapter extends App {
    final val StartTime: String = Instant.now().toString()
    final val AdapterName: String = sys.env.getOrElse(
        GitlabConstants.EnvironmentApplicationName,
        GitlabConstants.DefaultApplicationName
    )
    final val ApiAddress: String = List(
        sys.env.getOrElse(GitlabConstants.EnvironmentHostName, GitlabConstants.DefaultHostName),
        sys.env.getOrElse(GitlabConstants.EnvironmentHostPort, GitlabConstants.DefaultHostPort)
    ).mkString(GitlabConstants.DoubleDot)

    // create or update a metadata document and start periodic updates
    Metadata.startMetadataTask()

    implicit val system: ActorSystem = ActorSystem("akka-http-sample")
    val shutDownHookThread: ShutdownHookThread = sys.addShutdownHook({
        val termination: Future[Terminated] = {
            Session.getSparkSession().stop()
            Metadata.stopMetadataTask()
            system.terminate()
        }
    })
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val routes = concat(
        new CommitDataService(system.actorOf(Props[CommitDataActor])).route,
        new TimestampService(system.actorOf(Props[TimestampActor])).route,
        new ProjectDataService(system.actorOf(Props[ProjectDataActor])).route,
        new InfoService(system.actorOf(Props[InfoActor])).route,
        SwaggerRoutes.getSwaggerRouter(SwaggerAdapterDocService)
    )

    val serverBinding: Future[Http.ServerBinding] =
        Http().newServerAt("0.0.0.0", GitlabConstants.HttpInternalPort).bindFlow(routes)
}
