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
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import spray.json.JsObject
import visdom.http.HttpConstants
import visdom.http.server.QueryOptionsBaseObject
import visdom.http.server.ServerConstants
import visdom.http.server.services.constants.GeneralAdapterDescriptions
import visdom.http.server.services.constants.GeneralAdapterExamples
import visdom.utils.WarningConstants


@SuppressWarnings(Array(WarningConstants.UnusedMethodParameter))
@Path(ServerConstants.UpdateRootPath)
class UpdateService(actorRef: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with AdapterService
{
    val route: Route = (
        getServiceRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = GeneralAdapterDescriptions.UpdateEndpointSummary,
        description = GeneralAdapterDescriptions.UpdateEndpointDescription,
        responses = Array(
            new ApiResponse(
                responseCode = HttpConstants.StatusOkCode,
                description = GeneralAdapterDescriptions.UpdateOkDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[JsObject]),
                        examples = Array(
                            new ExampleObject(
                                name = GeneralAdapterExamples.UpdateExampleName,
                                value = GeneralAdapterExamples.UpdateExample
                            )
                        )
                    )
                )
            )
        )
    )
    def getServiceRoute: RequestContext => Future[RouteResult] = (
        path(ServerConstants.UpdatePath)
    ) {
        getRoute(actorRef, QueryOptionsBaseObject)
    }
}
