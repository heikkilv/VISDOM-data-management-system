package visdom.adapters.dataset

import akka.actor.Props
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import visdom.http.server.actors.DatasetMultiActor
import visdom.http.server.actors.DatasetSingleActor
import visdom.http.server.actors.DatasetUpdateActor
import visdom.http.server.actors.GeneralAdapterInfoActor
import visdom.http.server.services.AdapterInfoService
import visdom.http.server.services.ArtifactService
import visdom.http.server.services.AuthorService
import visdom.http.server.services.EventService
import visdom.http.server.services.MetadataService
import visdom.http.server.services.OriginService
import visdom.http.server.services.SingleService
import visdom.http.server.services.UpdateService
import visdom.http.server.swagger.SwaggerAdapterDocService
import visdom.http.server.swagger.SwaggerRoutes


object AdapterRoutes extends visdom.adapters.AdapterRoutes {
    override val routes: Route = Directives.concat(
        new AdapterInfoService(system.actorOf(Props[GeneralAdapterInfoActor])).route,
        new SingleService(system.actorOf(Props[DatasetSingleActor])).route,
        new OriginService(system.actorOf(Props[DatasetMultiActor])).route,
        new EventService(system.actorOf(Props[DatasetMultiActor])).route,
        new AuthorService(system.actorOf(Props[DatasetMultiActor])).route,
        new ArtifactService(system.actorOf(Props[DatasetMultiActor])).route,
        new MetadataService(system.actorOf(Props[DatasetMultiActor])).route,
        new UpdateService(system.actorOf(Props[DatasetUpdateActor])).route,
        SwaggerRoutes.getSwaggerRouter(
            new SwaggerAdapterDocService(
                Adapter.adapterValues,
                Set(
                    classOf[AdapterInfoService],
                    classOf[SingleService],
                    classOf[OriginService],
                    classOf[EventService],
                    classOf[AuthorService],
                    classOf[ArtifactService],
                    classOf[MetadataService],
                    classOf[UpdateService]
                )
            )
        )
    )
}
