package visdom.broker

import akka.actor.ActorSystem
import akka.actor.Terminated
import akka.http.scaladsl.Http
import scala.concurrent.Future
import scala.concurrent.ExecutionContextExecutor
import scala.sys.ShutdownHookThread
import visdom.http.server.ServerConstants


object DataBroker extends App
{
    // create or update a metadata document and start periodic updates
    Metadata.startMetadataTask()

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
            .bindFlow(BrokerValues.routes)
}
