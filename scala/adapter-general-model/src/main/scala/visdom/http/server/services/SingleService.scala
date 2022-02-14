package visdom.http.server.services

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
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
import visdom.http.server.ServerConstants
import visdom.http.server.options.SingleOptions
import visdom.http.server.response.ResponseProblem
import visdom.http.server.services.constants.GeneralAdapterConstants
import visdom.http.server.services.constants.GeneralAdapterDescriptions
import visdom.http.server.services.constants.GeneralAdapterExamples
import visdom.utils.WarningConstants


// scalastyle:off method.length
@SuppressWarnings(Array(WarningConstants.UnusedMethodParameter))
@Path(ServerConstants.SingleRootPath)
class SingleService(actorRef: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with AdapterService
{
    val route: Route = (
        getServiceRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = GeneralAdapterDescriptions.SingleEndpointSummary,
        description = GeneralAdapterDescriptions.SingleEndpointDescription,
        parameters = Array(
            new Parameter(
                name = GeneralAdapterConstants.Type,
                in = ParameterIn.QUERY,
                required = false,
                description = GeneralAdapterDescriptions.DescriptionType,
                schema = new Schema(
                    implementation = classOf[String],
                    defaultValue = GeneralAdapterConstants.DefaultType
                )
            ),
            new Parameter(
                name = GeneralAdapterConstants.Uuid,
                in = ParameterIn.QUERY,
                required = false,
                description = GeneralAdapterDescriptions.DescriptionUuid,
                schema = new Schema(
                    implementation = classOf[String],
                    defaultValue = GeneralAdapterConstants.DefaultUuid
                )
            )
        ),
        responses = Array(
            new ApiResponse(
                responseCode = HttpConstants.StatusOkCode,
                description = GeneralAdapterDescriptions.SingleStatusOkDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[JsObject]),
                        examples = Array(
                            new ExampleObject(
                                name = GeneralAdapterExamples.SingleExampleOkName,
                                value = GeneralAdapterExamples.SingleExampleOk
                            )
                        )
                    )
                )
            ),
            new ApiResponse(
                responseCode = HttpConstants.StatusNotFoundCode,
                description = GeneralAdapterDescriptions.SingleNotFoundDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[JsObject]),
                        examples = Array(
                            new ExampleObject(
                                name = GeneralAdapterExamples.SingleExampleNotFoundName,
                                value = GeneralAdapterExamples.SingleExampleNotFound
                            )
                        )
                    )
                )
            ),
            new ApiResponse(
                responseCode = HttpConstants.StatusErrorCode,
                description = ServerConstants.StatusErrorDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[ResponseProblem]),
                        examples = Array(
                            new ExampleObject(
                                name = ServerConstants.ResponseExampleErrorName,
                                value = ServerConstants.ResponseExampleError
                            )
                        )
                    )
                )
            )
        )
    )
    def getServiceRoute: RequestContext => Future[RouteResult] = (
        path(ServerConstants.SinglePath) &
        parameters(
            GeneralAdapterConstants.Type.withDefault(GeneralAdapterConstants.DefaultType),
            GeneralAdapterConstants.Uuid.withDefault(GeneralAdapterConstants.DefaultUuid)
        )
    ) {
        (
            objectType,
            uuid
        ) => get {
            getRoute(
                actorRef,
                SingleOptions(
                    objectType = objectType,
                    uuid = uuid
                )
            )
        }
    }
}
// scalastyle:on method.length
