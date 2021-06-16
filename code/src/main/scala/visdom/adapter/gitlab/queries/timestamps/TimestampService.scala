package visdom.adapter.gitlab.queries.timestamps

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult
import akka.pattern.ask
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import java.util.concurrent.TimeoutException
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import spray.json.JsObject
import visdom.adapter.gitlab.queries.Constants
import visdom.adapter.gitlab.queries.GitlabProtocol
import visdom.adapter.gitlab.queries.GitlabResponse
import visdom.adapter.gitlab.queries.GitlabResponseOk
import visdom.adapter.gitlab.queries.GitlabResponseProblem


// scalastyle:off method.length
@SuppressWarnings(Array("UnusedMethodParameter"))
@Path(TimestampConstants.TimestampRootPath)
class TimestampService(commitDataActor: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with GitlabProtocol {
    val route: Route = (
        getTimestampRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = TimestampConstants.TimestampEndpointSummary,
        description = TimestampConstants.TimestampEndpointDescription,
        parameters = Array(
            new Parameter(
                name = Constants.ParameterFilePaths,
                in = ParameterIn.QUERY,
                required = true,
                description = Constants.ParameterDescriptionFilePaths,
                example = Constants.ParameterExampleFilePaths
            ),
            new Parameter(
                name = Constants.ParameterProjectName,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionProjectName,
                example = Constants.ParameterExampleProjectName
            ),
            new Parameter(
                name = Constants.ParameterStartDate,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionStartDateTime,
                schema = new Schema(
                    implementation = classOf[String],
                    format = Constants.DateTimeFormat
                )
            ),
            new Parameter(
                name = Constants.ParameterEndDate,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionEndDateTime,
                schema = new Schema(
                    implementation = classOf[String],
                    format = Constants.DateTimeFormat
                )
            )
        ),
        responses = Array(
            new ApiResponse(
                responseCode = Constants.StatusOkCode,
                description = TimestampConstants.TimestampStatusOkDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[JsObject]),
                        examples = Array(
                            new ExampleObject(
                                name = TimestampConstants.ResponseExampleOkName,
                                value = TimestampConstants.TimestampResponseExampleOk
                            )
                        )
                    )
                )
            ),
            new ApiResponse(
                responseCode = Constants.StatusInvalidCode,
                description = Constants.StatusInvalidDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[GitlabResponseProblem]),
                        examples = Array(
                            new ExampleObject(
                                name = Constants.ResponseExampleInvalidName,
                                value = Constants.ResponseExampleInvalid
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
                        schema = new Schema(implementation = classOf[GitlabResponseProblem]),
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
                        schema = new Schema(implementation = classOf[GitlabResponseProblem]),
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
    def getTimestampRoute: RequestContext => Future[RouteResult] = (
        path(TimestampConstants.TimestampPath) &
        parameters(
            Constants.ParameterFilePaths.withDefault(""),
            Constants.ParameterProjectName.optional,
            Constants.ParameterStartDate.optional,
            Constants.ParameterEndDate.optional
        )
    ) {
        (
            filePaths,
            projectName,
            startDate,
            endDate
        ) => get {
            val response: GitlabResponse = try {
                Await.result(
                    (commitDataActor ? TimestampQueryOptionsSimple(
                        filePaths,
                        projectName,
                        startDate,
                        endDate
                    )).mapTo[GitlabResponse],
                    maxWaitTime
                )
            } catch  {
                case error: TimeoutException => GitlabResponseProblem(
                    Constants.QueryErrorStatus,
                    error.getMessage()
                )
            }

            response match {
                case okResponse: GitlabResponseOk =>
                    complete(StatusCodes.OK, okResponse.data)
                case problemResponse: GitlabResponseProblem => complete(
                    problemResponse.status match {
                        case Constants.QueryInvalidStatus => StatusCodes.BadRequest
                        case Constants.QueryNotFoundStatus => StatusCodes.NotFound
                        case Constants.QueryErrorStatus => StatusCodes.InternalServerError
                    },
                    problemResponse
                )
            }
        }
    }
}
// scalastyle:on method.length
