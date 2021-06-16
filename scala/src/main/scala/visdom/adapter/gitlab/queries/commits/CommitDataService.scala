package visdom.adapter.gitlab.queries.commits

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
@Path(CommitDataConstants.CommitDataRootPath)
class CommitDataService(commitDataActor: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with GitlabProtocol {
    val route: Route = (
        getCommitDataRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = CommitDataConstants.CommitDataEndpointSummary,
        description = CommitDataConstants.CommitDataEndpointDescription,
        parameters = Array(
            new Parameter(
                name = Constants.ParameterProjectName,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionProjectName,
                example = Constants.ParameterExampleProjectName
            ),
            new Parameter(
                name = Constants.ParameterUserName,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionUserName,
                example = Constants.ParameterExampleUserName
            ),
            new Parameter(
                name = Constants.ParameterStartDate,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionStartDate,
                schema = new Schema(
                    implementation = classOf[String],
                    format = Constants.DateFormat
                )
            ),
            new Parameter(
                name = Constants.ParameterEndDate,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionEndDate,
                schema = new Schema(
                    implementation = classOf[String],
                    format = Constants.DateFormat
                )
            )
        ),
        responses = Array(
            new ApiResponse(
                responseCode = Constants.StatusOkCode,
                description = CommitDataConstants.CommitDataStatusOkDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[JsObject]),
                        examples = Array(
                            new ExampleObject(
                                name = CommitDataConstants.ResponseExampleOkName,
                                value = CommitDataConstants.CommitDataResponseExampleOk
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
    def getCommitDataRoute: RequestContext => Future[RouteResult] = (
        path(CommitDataConstants.CommitDataPath) &
        parameters(
            Constants.ParameterProjectName.optional,
            Constants.ParameterUserName.optional,
            Constants.ParameterStartDate.optional,
            Constants.ParameterEndDate.optional
        )
    ) {
        (
            projectName,
            userName,
            startDate,
            endDate
        ) => get {
            val response: GitlabResponse = try {
                Await.result(
                    (commitDataActor ? CommitDataQueryOptions(
                        projectName,
                        userName,
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
