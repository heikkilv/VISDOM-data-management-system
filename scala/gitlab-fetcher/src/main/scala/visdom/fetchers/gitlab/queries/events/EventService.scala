package visdom.fetchers.gitlab.queries.events

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
import visdom.fetchers.gitlab.queries.Constants
import visdom.http.server.response.ResponseAccepted
import visdom.http.server.response.ResponseProblem
import visdom.http.server.fetcher.gitlab.EventQueryOptions
import visdom.http.server.GitlabFetcherResponseHandler


// scalastyle:off method.length
@Path(EventConstants.EventRootPath)
class EventService(eventActor: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with GitlabFetcherResponseHandler {
    val route: Route = (
        getEventRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = EventConstants.EventEndpointSummary,
        description = EventConstants.EventEndpointDescription,
        parameters = Array(
            new Parameter(
                name = Constants.ParameterUserId,
                in = ParameterIn.QUERY,
                required = true,
                description = Constants.ParameterDescriptionUserId,
                example = Constants.ParameterExampleUserId
            ),
            new Parameter(
                name = Constants.ParameterActionType,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionActionType,
                schema = new Schema(
                    implementation = classOf[String],
                    allowableValues = Array(
                        Constants.ActionTypeApproved,
                        Constants.ActionTypeClosed,
                        Constants.ActionTypeCommented,
                        Constants.ActionTypeCreated,
                        Constants.ActionTypeDestroyed,
                        Constants.ActionTypeExpired,
                        Constants.ActionTypeJoined,
                        Constants.ActionTypeLeft,
                        Constants.ActionTypeMerged,
                        Constants.ActionTypePushed,
                        Constants.ActionTypeReopened,
                        Constants.ActionTypeUpdated
                    )
                )
            ),
            new Parameter(
                name = Constants.ParameterTargetType,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionTargetType,
                schema = new Schema(
                    implementation = classOf[String],
                    allowableValues = Array(
                        Constants.TargetTypeIssue,
                        Constants.TargetTypeMilestone,
                        Constants.TargetTypeMergeRequest,
                        Constants.TargetTypeNote,
                        Constants.TargetTypeProject,
                        Constants.TargetTypeSnippet,
                        Constants.TargetTypeUser
                    )
                )
            ),
            new Parameter(
                name = Constants.ParameterDateAfter,
                in = ParameterIn.QUERY,
                required = false,
                description = EventConstants.ParameterDescriptionDateAfter,
                schema = new Schema(
                    implementation = classOf[String],
                    format = Constants.DateFormat
                )
            ),
            new Parameter(
                name = Constants.ParameterDateBefore,
                in = ParameterIn.QUERY,
                required = false,
                description = EventConstants.ParameterDescriptionDateBefore,
                schema = new Schema(
                    implementation = classOf[String],
                    format = Constants.DateFormat
                )
            ),
            new Parameter(
                name = Constants.ParameterUseAnonymization,
                in = ParameterIn.QUERY,
                required = false,
                description = Constants.ParameterDescriptionUseAnonymization,
                schema = new Schema(
                    implementation = classOf[String],
                    defaultValue = Constants.ParameterDefaultUseAnonymization,
                    allowableValues = Array(Constants.FalseString, Constants.TrueString)
                )
            )
        ),
        responses = Array(
            new ApiResponse(
                responseCode = Constants.StatusAcceptedCode,
                description = EventConstants.EventStatusAcceptedDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[ResponseAccepted]),
                        examples = Array(
                            new ExampleObject(
                                name = Constants.ResponseExampleAcceptedName,
                                value = EventConstants.EventResponseExampleAccepted
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
                        schema = new Schema(implementation = classOf[ResponseProblem]),
                        examples = Array(
                            new ExampleObject(
                                name = Constants.ResponseExampleInvalidName4,
                                value = Constants.ResponseExampleInvalid4
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
    def getEventRoute: RequestContext => Future[RouteResult] = (
        path(EventConstants.EventPath) &
        parameters(
            Constants.ParameterUserId.withDefault(""),
            Constants.ParameterActionType.optional,
            Constants.ParameterTargetType.optional,
            Constants.ParameterDateAfter.optional,
            Constants.ParameterDateBefore.optional,
            Constants.ParameterUseAnonymization
                .withDefault(Constants.ParameterDefaultUseAnonymization)
        )
    ) {
        (
            userId,
            actionType,
            targetType,
            dateAfter,
            dateBefore,
            useAnonymization
        ) => get {
            val options: EventQueryOptions = EventQueryOptions(
                userId,
                actionType,
                targetType,
                dateAfter,
                dateBefore,
                useAnonymization
            )
            getRoute(eventActor, options)
        }
    }
}
// scalastyle:on method.length
