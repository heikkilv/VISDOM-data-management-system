package visdom.adapter.gitlab.queries.projects

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
import visdom.adapter.gitlab.queries.Constants
import visdom.http.server.GitlabAdapterResponseHandler
import visdom.http.server.QueryOptionsBase
import visdom.http.server.response.ResponseProblem
import visdom.utils.WarningConstants.UnusedMethodParameter


// scalastyle:off method.length
@SuppressWarnings(Array(UnusedMethodParameter))
@Path(ProjectDataConstants.ProjectDataRootPath)
class ProjectDataService(projectDataActor: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with GitlabAdapterResponseHandler {
    val route: Route = (
        getProjectDataRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = ProjectDataConstants.ProjectDataEndpointSummary,
        description = ProjectDataConstants.ProjectDataEndpointDescription,
        responses = Array(
            new ApiResponse(
                responseCode = Constants.StatusOkCode,
                description = ProjectDataConstants.ProjectDataStatusOkDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[JsObject]),
                        examples = Array(
                            new ExampleObject(
                                name = ProjectDataConstants.ResponseExampleOkName,
                                value = ProjectDataConstants.ProjectDataResponseExampleOk
                            )
                        )
                    )
                )
            ),
            new ApiResponse(
                responseCode = Constants.StatusNotFoundCode,
                description = Constants.StatusNotFoundDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[ResponseProblem]),
                        examples = Array(
                            new ExampleObject(
                                name = Constants.ResponseExampleNotFoundName,
                                value = Constants.ResponseExampleNotFound
                            )
                        )
                    )
                )
            ),
            new ApiResponse(
                responseCode = Constants.StatusErrorCode,
                description = Constants.StatusErrorDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[ResponseProblem]),
                        examples = Array(
                            new ExampleObject(
                                name = Constants.ResponseExampleErrorName,
                                value = Constants.ResponseExampleError
                            )
                        )
                    )
                )
            )
        )
    )
    def getProjectDataRoute: RequestContext => Future[RouteResult] = (
        path(ProjectDataConstants.ProjectDataPath)
    ) {
        get {
            getRoute(projectDataActor, new QueryOptionsBase())
        }
    }
}
// scalastyle:on method.length
