package visdom.fetchers.gitlab.queries.files

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
@Path(FileConstants.FileRootPath)
class FileService(commitActor: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with FileProtocol {
    val route: Route = (
        getCommitRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = FileConstants.FileEndpointSummary,
        description = FileConstants.FileEndpointDescription,
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
                name = Constants.ParameterFilePath,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionFilePath
            ),
            new Parameter(
                name = Constants.ParameterRecursive,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionRecursive,
                schema = new Schema(
                    implementation = classOf[String],
                    defaultValue = Constants.ParameterDefaultRecursiveString,
                    allowableValues = Array(Constants.FalseString, Constants.TrueString)
                )
            ),
            new Parameter(
                name = Constants.ParameterIncludeCommitLinks,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionIncludeCommitLinks,
                schema = new Schema(
                    implementation = classOf[String],
                    defaultValue = Constants.ParameterDefaultIncludeCommitLinksString,
                    allowableValues = Array(Constants.FalseString, Constants.TrueString)
                )
            )
        ),
        responses = Array(
            new ApiResponse(
                responseCode = Constants.StatusAcceptedCode,
                description = FileConstants.FileStatusAcceptedDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[GitlabResponseAccepted[FileQueryOptions]]),
                        examples = Array(
                            new ExampleObject(
                                name = Constants.ResponseExampleAcceptedName,
                                value = FileConstants.FileResponseExampleAccepted
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
    def getCommitRoute: RequestContext => Future[RouteResult] = (
        path(FileConstants.FilePath) &
        parameters(
            Constants.ParameterProjectName.withDefault(""),
            Constants.ParameterReference
                .withDefault(Constants.ParameterDefaultReference),
            Constants.ParameterFilePath.optional,
            Constants.ParameterRecursive
                .withDefault(Constants.ParameterDefaultRecursiveString),
            Constants.ParameterIncludeCommitLinks
                .withDefault(Constants.ParameterDefaultIncludeCommitLinksString)
        )
    ) {
        (
            projectName,
            reference,
            path,
            recursive,
            includeCommitLinks
        ) => get {
            val response: GitlabResponse = try {
                Await.result(
                    (
                        commitActor ? FileQueryOptions(
                            projectName,
                            reference,
                            path,
                            recursive,
                            includeCommitLinks
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
                case acceptedResponse: GitlabResponseAccepted[FileQueryOptions] @unchecked =>
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
