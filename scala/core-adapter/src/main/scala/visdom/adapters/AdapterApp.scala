// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters

import akka.actor.ActorSystem
import akka.actor.Terminated
import akka.http.scaladsl.Http
import scala.concurrent.Future
import scala.sys.ShutdownHookThread
import visdom.http.server.ServerConstants


trait AdapterApp extends App
{
    // org.apache.log4j.BasicConfigurator.configure()
    // org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.INFO)

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
