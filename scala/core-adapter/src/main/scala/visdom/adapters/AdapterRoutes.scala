// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import scala.concurrent.ExecutionContextExecutor
import visdom.http.server.ServerConstants


trait AdapterRoutes {
    implicit val system: ActorSystem = ActorSystem(ServerConstants.DefaultActorSystem)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val routes: Route
}

object DefaultAdapterRoutes extends AdapterRoutes {
    val routes: Route = Directives.concat()
}
