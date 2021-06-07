package visdom.fetchers.gitlab.queries.all

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
import visdom.fetchers.gitlab.queries.Constants
import visdom.fetchers.gitlab.queries.GitlabResponse
import visdom.fetchers.gitlab.queries.GitlabResponseAccepted
import visdom.fetchers.gitlab.queries.GitlabResponseProblem


// scalastyle:off method.length
@SuppressWarnings(Array("UnusedMethodParameter"))
@Path(AllDataConstants.AllDataRootPath)
class AllDataService(allDataActor: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with AllDataProtocol {
    val route: Route = (
        getAllDataRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = AllDataConstants.AllDataEndpointSummary,
        description = AllDataConstants.AllDataEndpointDescription,
        parameters = Array(
            new Parameter(
                name = Constants.ParameterProjectName,
                in = ParameterIn.QUERY,
                required = true,
                description = Constants.ParameterDescriptionProjectName,
                example = Constants.ParameterExampleProjectName
            ),
            new Parameter(
                name = Constants.ParameterReference,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionReference,
                schema = new Schema(
                    implementation = classOf[String],
                    defaultValue = Constants.ParameterDefaultReference
                )
            ),
            new Parameter(
                name = Constants.ParameterStartDate,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionStartDate,
                schema = new Schema(
                    implementation = classOf[String],
                    format = Constants.DateTimeFormat
                )
            ),
            new Parameter(
                name = Constants.ParameterEndDate,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionEndDate,
                schema = new Schema(
                    implementation = classOf[String],
                    format = Constants.DateTimeFormat
                )
            )
        ),
        responses = Array(
            new ApiResponse(
                responseCode = Constants.StatusAcceptedCode,
                description = AllDataConstants.AllDataStatusAcceptedDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[GitlabResponseAccepted[AllDataQueryOptions]]),
                        examples = Array(
                            new ExampleObject(
                                name = Constants.ResponseExampleAcceptedName,
                                value = AllDataConstants.AllDataResponseExampleAccepted
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
                                name = Constants.ResponseExampleInvalidName1,
                                value = Constants.ResponseExampleInvalid1
                            ),
                            new ExampleObject(
                                name = Constants.ResponseExampleInvalidName2,
                                value = Constants.ResponseExampleInvalid2
                            )
                        )
                    )
                )
            ),
            new ApiResponse(
                responseCode = Constants.StatusUnauthorizedCode,
                description = Constants.StatusUnauthorizedDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[GitlabResponseProblem]),
                        examples = Array(
                            new ExampleObject(
                                name = Constants.ResponseExampleUnauthorizedName,
                                value = Constants.ResponseExampleUnauthorized
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
    def getAllDataRoute: RequestContext => Future[RouteResult] = (
        path(AllDataConstants.AllDataPath) &
        parameters(
            Constants.ParameterProjectName.withDefault(""),
            Constants.ParameterReference
                .withDefault(Constants.ParameterDefaultReference),
            Constants.ParameterStartDate.optional,
            Constants.ParameterEndDate.optional
        )
    ) {
        (
            projectName,
            reference,
            startDate,
            endDate
        ) => get {
            val response: GitlabResponse = try {
                Await.result(
                    (
                        allDataActor ? AllDataQueryOptions(
                            projectName,
                            reference,
                            startDate,
                            endDate
                        )
                    ).mapTo[GitlabResponse],
                    maxWaitTime
                )
            } catch  {
                case error: TimeoutException => GitlabResponseProblem(
                    Constants.QueryErrorStatus,
                    error.getMessage()
                )
            }

            response match {
                case acceptedResponse: GitlabResponseAccepted[AllDataQueryOptions] @unchecked =>
                    complete(StatusCodes.Accepted, acceptedResponse)
                case problemResponse: GitlabResponseProblem => problemResponse.status match {
                    case Constants.QueryInvalidStatus =>
                        complete(StatusCodes.BadRequest, problemResponse)
                    case Constants.QueryUnauthorizedStatus =>
                        complete(StatusCodes.Unauthorized, problemResponse)
                    case Constants.QueryNotFoundStatus =>
                        complete(StatusCodes.NotFound, problemResponse)
                    case Constants.QueryErrorStatus =>
                        complete(StatusCodes.InternalServerError, problemResponse)
                }
            }
        }
    }
}
// scalastyle:on method.length
