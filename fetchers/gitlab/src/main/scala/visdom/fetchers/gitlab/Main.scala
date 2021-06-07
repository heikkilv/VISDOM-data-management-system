package visdom.fetchers.gitlab

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Terminated
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.concat
import java.time.Instant
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.sys.ShutdownHookThread
import visdom.fetchers.gitlab.queries.all.AllDataActor
import visdom.fetchers.gitlab.queries.all.AllDataService
import visdom.fetchers.gitlab.queries.commits.CommitActor
import visdom.fetchers.gitlab.queries.commits.CommitService
import visdom.fetchers.gitlab.queries.files.FileActor
import visdom.fetchers.gitlab.queries.files.FileService
import visdom.fetchers.gitlab.queries.info.InfoActor
import visdom.fetchers.gitlab.queries.info.InfoService


object Main extends App with SwaggerUiSite
{
    val startTime: String = Instant.now().toString()

    Routes.storeMetadata()

    implicit val system: ActorSystem = ActorSystem("akka-http-sample")
    val shutDownHookThread: ShutdownHookThread = sys.addShutdownHook({
        val termination: Future[Terminated] = system.terminate()
    })
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val routes = concat(
        new AllDataService(system.actorOf(Props[AllDataActor])).route,
        new CommitService(system.actorOf(Props[CommitActor])).route,
        new FileService(system.actorOf(Props[FileActor])).route,
        new InfoService(system.actorOf(Props[InfoActor])).route,
        SwaggerDocService.routes,
        swaggerUiSiteRoute
    )
    Http().bindAndHandle(routes, "0.0.0.0", GitlabConstants.HttpInternalPort)
}
