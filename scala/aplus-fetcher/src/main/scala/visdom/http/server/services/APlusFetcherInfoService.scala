package visdom.http.server.services

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import visdom.http.HttpConstants
import visdom.http.server.QueryOptionsBaseObject
import visdom.http.server.APlusFetcherResponseHandler
import visdom.http.server.ServerConstants
import visdom.http.server.response.APlusFetcherInfoResponse
import visdom.http.server.services.constants.Descriptions
import visdom.http.server.services.constants.Examples
import visdom.utils.WarningConstants.UnusedMethodParameter


@SuppressWarnings(Array(UnusedMethodParameter))
@Path(ServerConstants.InfoRootPath)
class APlusFetcherInfoService(infoActor: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with APlusFetcherResponseHandler {
    val route: Route = (
        getInfoRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = constants.APlusFetcherDescriptions.APlusFetcherInfoEndpointSummary,
        description = constants.APlusFetcherDescriptions.APlusFetcherInfoEndpointDescription,
        responses = Array(
            new ApiResponse(
                responseCode = HttpConstants.StatusOkCode,
                description = Descriptions.InfoStatusOkDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[APlusFetcherInfoResponse]),
                        examples = Array(
                            new ExampleObject(
                                name = Examples.InfoResponseExampleName,
                                value = constants.APlusFetcherExamples.APlusFetcherInfoResponseExample
                            )
                        )
                    )
                )
            )
        )
    )
    def getInfoRoute: RequestContext => Future[RouteResult] = (
        path(ServerConstants.InfoPath)
    ) {
        get {
            getRoute(infoActor, QueryOptionsBaseObject)
        }
    }
}
