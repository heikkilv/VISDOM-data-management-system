package visdom.fetchers.gitlab

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Terminated
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.concat
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.sys.ShutdownHookThread
import visdom.fetchers.gitlab.queries.commits.CommitActor
import visdom.fetchers.gitlab.queries.commits.CommitService


object Main extends App with SwaggerUiSite
{
    Routes.storeMetadata()

    implicit val system: ActorSystem = ActorSystem("akka-http-sample")
    val shutDownHookThread: ShutdownHookThread = sys.addShutdownHook({
        val termination: Future[Terminated] = system.terminate()
    })
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val commitReference = system.actorOf(Props[CommitActor])
    val routes = concat(
        new CommitService(commitReference).route,
        SwaggerDocService.routes,
        swaggerUiSiteRoute
    )
    Http().bindAndHandle(routes, "0.0.0.0", 8080)
}
