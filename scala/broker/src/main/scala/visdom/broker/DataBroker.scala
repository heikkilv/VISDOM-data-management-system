// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
