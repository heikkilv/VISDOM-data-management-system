package visdom.adapters

import akka.actor.ActorSystem
import akka.actor.Props
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import scala.concurrent.ExecutionContextExecutor
import visdom.http.server.ServerConstants
import visdom.http.server.actors.AdapterInfoActor
import visdom.http.server.services.InfoService
import visdom.http.server.swagger.SwaggerAdapterDocService
import visdom.http.server.swagger.SwaggerRoutes


trait AdapterRoutes {
    implicit val system: ActorSystem = ActorSystem(ServerConstants.DefaultActorSystem)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val routes: Route = Directives.concat(
        new InfoService(system.actorOf(Props[AdapterInfoActor])).route,
        SwaggerRoutes.getSwaggerRouter(
            new SwaggerAdapterDocService(
                AdapterApp.adapterApp.adapterValues,
                Set(
                    classOf[InfoService]
                )
            )
        )
    )
}

object DefaultAdapterRoutes extends AdapterRoutes
