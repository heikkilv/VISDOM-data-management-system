package visdom.adapters

import akka.actor.ActorSystem
import akka.actor.Terminated
import akka.http.scaladsl.Http
import scala.concurrent.Future
import scala.sys.ShutdownHookThread
import visdom.http.server.ServerConstants


trait AdapterApp extends App
{
    org.apache.log4j.BasicConfigurator.configure()

    val adapterValues: AdapterValues
    val adapterRoutes: AdapterRoutes
    val adapterMetadata: Metadata

    def start(): Unit = {
        // create or update a metadata document and start periodic updates
        adapterMetadata.startMetadataTask()

        implicit val system: ActorSystem = ActorSystem(ServerConstants.DefaultActorSystem)
        val shutDownHookThread: ShutdownHookThread = sys.addShutdownHook({
            val termination: Future[Terminated] = {
                adapterMetadata.stopMetadataTask()
                system.terminate()
            }
        })

        val serverBinding: Future[Http.ServerBinding] =
            Http()
                .newServerAt(ServerConstants.HttpInternalHost, ServerConstants.HttpInternalPort)
                .bindFlow(adapterRoutes.routes)
    }
}

object DefaultAdapterApp extends AdapterApp {
    val adapterValues: AdapterValues = DefaultAdapterValues
    val adapterRoutes: AdapterRoutes = DefaultAdapterRoutes
    val adapterMetadata: Metadata = DefaultMetadata
}

object AdapterApp {
    val adapterApp: AdapterApp = DefaultAdapterApp
}
