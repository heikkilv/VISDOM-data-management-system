package visdom.fetchers.aplus

import akka.actor.ActorSystem
import akka.actor.Terminated
import akka.http.scaladsl.Http
import scala.concurrent.Future
import scala.sys.ShutdownHookThread
import visdom.http.server.ServerConstants
import visdom.utils.TaskUtils


object APlusFetcher extends App
{
    // create or update a metadata document and start periodic updates
    Metadata.startMetadataTask()

    // start the periodic execution of tasks related to GitLab data fetching
    TaskUtils.startTaskLoop(FetcherValues.gitlabTaskList)

    implicit val system: ActorSystem = ActorSystem(ServerConstants.DefaultActorSystem)
    val shutDownHookThread: ShutdownHookThread = sys.addShutdownHook({
        val termination: Future[Terminated] = {
            Metadata.stopMetadataTask()
            system.terminate()
        }
    })

    val serverBinding: Future[Http.ServerBinding] =
        Http()
            .newServerAt(ServerConstants.HttpInternalHost, ServerConstants.HttpInternalPort)
            .bindFlow(FetcherValues.routes)
}
