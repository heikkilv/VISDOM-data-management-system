package visdom.adapters.general

import akka.actor.Props
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import visdom.http.server.actors.AdapterInfoActor
import visdom.http.server.services.AdapterInfoService
import visdom.http.server.swagger.SwaggerAdapterDocService
import visdom.http.server.swagger.SwaggerRoutes


object AdapterRoutes extends visdom.adapters.AdapterRoutes {
    override val routes: Route = Directives.concat(
        new AdapterInfoService(system.actorOf(Props[AdapterInfoActor])).route,
        SwaggerRoutes.getSwaggerRouter(
            new SwaggerAdapterDocService(
                Adapter.adapterValues,
                Set(
                    classOf[AdapterInfoService]
                )
            )
        )
    )
}
