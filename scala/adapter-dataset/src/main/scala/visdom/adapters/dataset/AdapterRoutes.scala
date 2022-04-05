package visdom.adapters.dataset

import akka.actor.Props
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import visdom.http.server.actors.DatasetAdapterInfoActor
import visdom.http.server.actors.DatasetMultiActor
import visdom.http.server.actors.DatasetSingleActor
import visdom.http.server.actors.DatasetUpdateActor
import visdom.http.server.options.DatasetMultiOptions
import visdom.http.server.services.AdapterInfoService
import visdom.http.server.services.DatasetAuthorService
import visdom.http.server.services.DatasetArtifactService
import visdom.http.server.services.DatasetEventService
import visdom.http.server.services.DatasetMetadataService
import visdom.http.server.services.DatasetOriginService
import visdom.http.server.services.SingleService
import visdom.http.server.services.UpdateService
import visdom.http.server.swagger.SwaggerAdapterDocService
import visdom.http.server.swagger.SwaggerRoutes


object AdapterRoutes extends visdom.adapters.AdapterRoutes {
    override val routes: Route = Directives.concat(
        new AdapterInfoService(system.actorOf(Props[DatasetAdapterInfoActor])).route,
        new SingleService(system.actorOf(Props[DatasetSingleActor])).route,
        new DatasetOriginService(system.actorOf(Props[DatasetMultiActor])).route,
        new DatasetEventService(system.actorOf(Props[DatasetMultiActor])).route,
        new DatasetAuthorService(system.actorOf(Props[DatasetMultiActor])).route,
        new DatasetArtifactService(system.actorOf(Props[DatasetMultiActor])).route,
        new DatasetMetadataService(system.actorOf(Props[DatasetMultiActor])).route,
        new UpdateService(system.actorOf(Props[DatasetUpdateActor])).route,
        SwaggerRoutes.getSwaggerRouter(
            new SwaggerAdapterDocService(
                Adapter.adapterValues,
                Set(
                    classOf[AdapterInfoService],
                    classOf[SingleService],
                    classOf[DatasetOriginService],
                    classOf[DatasetEventService],
                    classOf[DatasetAuthorService],
                    classOf[DatasetArtifactService],
                    classOf[DatasetMetadataService],
                    classOf[UpdateService]
                )
            )
        )
    )
}
