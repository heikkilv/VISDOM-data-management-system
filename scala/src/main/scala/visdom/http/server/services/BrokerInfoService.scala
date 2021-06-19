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
// import visdom.fetchers.gitlab.queries.Constants
import visdom.http.server.BaseOptions
import visdom.http.server.ResponseUtils
import visdom.http.server.ServerConstants
import visdom.http.server.ServerProtocol
import visdom.http.server.response.GitlabFetcherInfoResponse
import visdom.http.server.services.constants.Descriptions
import visdom.http.server.services.constants.Examples
import visdom.utils.WarningConstants.UnusedMethodParameter
import visdom.http.HttpConstants


@SuppressWarnings(Array(UnusedMethodParameter))
@Path(ServerConstants.InfoRootPath)
class BrokerInfoService(infoActor: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with ServerProtocol {
    val route: Route = (
        getInfoRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = Descriptions.BrokerInfoEndpointSummary,
        description = Descriptions.BrokerInfoEndpointDescription,
        responses = Array(
            new ApiResponse(
                responseCode = HttpConstants.StatusOkCode,
                description = Descriptions.InfoStatusOkDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[GitlabFetcherInfoResponse]),
                        examples = Array(
                            new ExampleObject(
                                name = Examples.InfoResponseExampleName,
                                value = Examples.BrokerInfoResponseExample
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
            ResponseUtils.getRoute(infoActor, BaseOptions)
        }
    }
}
