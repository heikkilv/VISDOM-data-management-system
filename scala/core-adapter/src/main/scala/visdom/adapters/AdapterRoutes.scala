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
