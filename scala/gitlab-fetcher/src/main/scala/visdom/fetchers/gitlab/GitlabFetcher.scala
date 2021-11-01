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
import visdom.fetchers.gitlab.queries.multi.MultiService
import visdom.fetchers.gitlab.queries.multi.MultiActor
import visdom.fetchers.gitlab.queries.pipelines.PipelinesActor
import visdom.fetchers.gitlab.queries.pipelines.PipelinesService
import visdom.http.server.ServerConstants
import visdom.http.server.swagger.SwaggerRoutes


object GitlabFetcher extends App
{
    val StartTime: String = Instant.now().toString()

    // create or update a metadata document and start periodic updates
    Routes.startMetadataTask()

    implicit val system: ActorSystem = ActorSystem(ServerConstants.DefaultActorSystem)
    val shutDownHookThread: ShutdownHookThread = sys.addShutdownHook({
        val termination: Future[Terminated] = {
            Routes.stopMetadataTask()
            system.terminate()
        }
    })

    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val routes = concat(
        new AllDataService(system.actorOf(Props[AllDataActor])).route,
        new CommitService(system.actorOf(Props[CommitActor])).route,
        new FileService(system.actorOf(Props[FileActor])).route,
        new InfoService(system.actorOf(Props[InfoActor])).route,
        new MultiService(system.actorOf(Props[MultiActor])).route,
        new PipelinesService(system.actorOf(Props[PipelinesActor])).route,
        SwaggerRoutes.getSwaggerRouter(SwaggerFetcherDocService)
    )

    val serverBinding: Future[Http.ServerBinding] =
        Http()
            .newServerAt(ServerConstants.HttpInternalHost, ServerConstants.HttpInternalPort)
            .bindFlow(routes)
}
