package visdom.adapters.general

import akka.actor.Props
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import visdom.http.server.actors.GeneralAdapterInfoActor
import visdom.http.server.actors.SingleActor
import visdom.http.server.actors.TestActor
import visdom.http.server.services.AdapterInfoService
import visdom.http.server.services.SingleService
import visdom.http.server.services.TestService
import visdom.http.server.swagger.SwaggerAdapterDocService
import visdom.http.server.swagger.SwaggerRoutes


object AdapterRoutes extends visdom.adapters.AdapterRoutes {
    override val routes: Route = Directives.concat(
        new AdapterInfoService(system.actorOf(Props[GeneralAdapterInfoActor])).route,
        new SingleService(system.actorOf(Props[SingleActor])).route,
        new TestService(system.actorOf(Props[TestActor])).route,
        SwaggerRoutes.getSwaggerRouter(
            new SwaggerAdapterDocService(
                Adapter.adapterValues,
                Set(
                    classOf[AdapterInfoService],
                    classOf[SingleService],
                    classOf[TestService]
                )
            )
        )
    )
}
