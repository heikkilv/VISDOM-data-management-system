package visdom.fetchers.gitlab.queries.commits

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
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.concurrent.Await


@Path(CommitConstants.CommitRootPath)
class CommitService(commitActor: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with CommitProtocol {
    val route: Route = (
        getCommitRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = CommitConstants.CommitEndpointSummary,
        description = CommitConstants.CommitEndpointDescription,
        parameters = Array(
            new Parameter(
                name = CommitConstants.ParameterProjectName,
                in = ParameterIn.QUERY,
                required = true,
                description = CommitConstants.ParameterDescriptionProjectName,
                example = CommitConstants.ParameterExampleProjectName
            ),
            new Parameter(
                name = CommitConstants.ParameterReference,
                in = ParameterIn.QUERY,
                required = false,
                description = CommitConstants.ParameterDescriptionReference,
                schema = new Schema(
                    implementation = classOf[String],
                    defaultValue = CommitConstants.ParameterDefaultReference
                )
            ),
            new Parameter(
                name = CommitConstants.ParameterStartDate,
                in = ParameterIn.QUERY,
                required = false,
                description = CommitConstants.ParameterDescriptionStartDate,
                schema = new Schema(
                    implementation = classOf[String],
                    format = CommitConstants.DateTimeFormat
                )
            ),
            new Parameter(
                name = CommitConstants.ParameterEndDate,
                in = ParameterIn.QUERY,
                required = false,
                description = CommitConstants.ParameterDescriptionEndDate,
                schema = new Schema(
                    implementation = classOf[String],
                    format = CommitConstants.DateTimeFormat
                )
            ),
            new Parameter(
                name = CommitConstants.ParameterFilePath,
                in = ParameterIn.QUERY,
                required = false,
                description = CommitConstants.ParameterDescriptionFilePath
            ),
            new Parameter(
                name = CommitConstants.ParameterIncludeStatistics,
                in = ParameterIn.QUERY,
                required = false,
                description = CommitConstants.ParameterDescriptionIncludeStatistics,
                schema = new Schema(
                    implementation = classOf[String],
                    defaultValue = CommitConstants.ParameterDefaultIncludeStatisticsString,
                    allowableValues = Array(CommitConstants.FalseString, CommitConstants.TrueString)
                )
            ),
            new Parameter(
                name = CommitConstants.ParameterIncludeFileLinks,
                in = ParameterIn.QUERY,
                required = false,
                description = CommitConstants.ParameterDescriptionIncludeFileLinks,
                schema = new Schema(
                    implementation = classOf[String],
                    defaultValue = CommitConstants.ParameterDefaultIncludeFileLinksString,
                    allowableValues = Array(CommitConstants.FalseString, CommitConstants.TrueString)
                )
            ),
            new Parameter(
                name = CommitConstants.ParameterIncludeReferenceLinks,
                in = ParameterIn.QUERY,
                required = false,
                description = CommitConstants.ParameterDescriptionIncludeReferenceLinks,
                schema = new Schema(
                    implementation = classOf[String],
                    defaultValue = CommitConstants.ParameterDefaultIncludeReferenceLinksString,
                    allowableValues = Array(CommitConstants.FalseString, CommitConstants.TrueString)
                )
            ),
        ),
        responses = Array(
            new ApiResponse(
                responseCode = CommitConstants.CommitStatusAcceptedCode,
                description = CommitConstants.CommitStatusAcceptedDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[CommitResponseAccepted]),
                        examples = Array(
                            new ExampleObject(
                                name = CommitConstants.CommitResponseExampleAcceptedName,
                                value = CommitConstants.CommitResponseExampleAccepted
                            )
                        )
                    )
                )
            ),
            new ApiResponse(
                responseCode = CommitConstants.CommitStatusInvalidCode,
                description = CommitConstants.CommitStatusInvalidDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[CommitResponseInvalid]),
                        examples = Array(
                            new ExampleObject(
                                name = CommitConstants.CommitResponseExampleInvalidName1,
                                value = CommitConstants.CommitResponseExampleInvalid1
                            ),
                            new ExampleObject(
                                name = CommitConstants.CommitResponseExampleInvalidName2,
                                value = CommitConstants.CommitResponseExampleInvalid2
                            )
                        )
                    )
                )
            ),
            new ApiResponse(
                responseCode = CommitConstants.CommitStatusUnauthorizedCode,
                description = CommitConstants.CommitStatusUnauthorizedDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[CommitResponseUnauthorized]),
                        examples = Array(
                            new ExampleObject(
                                name = CommitConstants.CommitResponseExampleUnauthorizedName,
                                value = CommitConstants.CommitResponseExampleUnauthorized
                            )
                        )
                    )
                )
            ),
            new ApiResponse(
                responseCode = CommitConstants.CommitStatusNotFoundCode,
                description = CommitConstants.CommitStatusNotFoundDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[CommitResponseNotFound]),
                        examples = Array(
                            new ExampleObject(
                                name = CommitConstants.CommitResponseExampleNotFoundName,
                                value = CommitConstants.CommitResponseExampleNotFound
                            )
                        )
                    )
                )
            ),
            new ApiResponse(
                responseCode = CommitConstants.CommitStatusErrorCode,
                description = CommitConstants.CommitStatusErrorDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[CommitResponseError]),
                        examples = Array(
                            new ExampleObject(
                                name = CommitConstants.CommitResponseExampleErrorName,
                                value = CommitConstants.CommitResponseExampleError
                            )
                        )
                    )
                )
            )
        )
    )
    def getCommitRoute: RequestContext => Future[RouteResult] = (
        path(CommitConstants.CommitPath) &
        parameters(
            CommitConstants.ParameterProjectName.withDefault(""),
            CommitConstants.ParameterReference
                .withDefault(CommitConstants.ParameterDefaultReference),
            CommitConstants.ParameterStartDate.optional,
            CommitConstants.ParameterEndDate.optional,
            CommitConstants.ParameterFilePath.optional,
            CommitConstants.ParameterIncludeStatistics
                .withDefault(CommitConstants.ParameterDefaultIncludeStatisticsString),
            CommitConstants.ParameterIncludeFileLinks
                .withDefault(CommitConstants.ParameterDefaultIncludeFileLinksString),
            CommitConstants.ParameterIncludeReferenceLinks
                .withDefault(CommitConstants.ParameterDefaultIncludeReferenceLinksString)
        )
    ) {
        (
            projectName,
            reference,
            startDate,
            endDate,
            path,
            includeStatistics,
            includeFileLinks,
            includeReferenceLinks
        ) => get {
            val response: CommitResponse = try {
                Await.result(
                    (
                        commitActor ? CommitQueryOptions(
                            projectName,
                            reference,
                            startDate,
                            endDate,
                            path,
                            includeStatistics,
                            includeFileLinks,
                            includeReferenceLinks
                        )
                    ).mapTo[CommitResponse],
                    maxWaitTime
                )
            } catch  {
                case error: TimeoutException => CommitResponseError(
                    CommitConstants.CommitQueryErrorStatus,
                    error.getMessage()
                )
            }

            response match {
                case acceptedResponse: CommitResponseAccepted =>
                    complete(StatusCodes.Accepted, acceptedResponse)
                case invalidResponse: CommitResponseInvalid =>
                    complete(StatusCodes.BadRequest, invalidResponse)
                case unauthorizedResponse: CommitResponseUnauthorized =>
                    complete(StatusCodes.Unauthorized, unauthorizedResponse)
                case notFoundResponse: CommitResponseNotFound =>
                    complete(StatusCodes.NotFound, notFoundResponse)
                case errorResponse: CommitResponseError=>
                    complete(StatusCodes.InternalServerError, errorResponse)
            }
        }
    }
}
