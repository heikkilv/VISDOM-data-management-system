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
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import spray.json.JsObject
import visdom.adapters.course.options.CommitQueryInput
import visdom.http.HttpConstants
import visdom.http.server.CourseAdapterResponseHandler
import visdom.http.server.ServerConstants
import visdom.http.server.response.ResponseProblem
import visdom.http.server.services.constants.CourseAdapterDescriptions
import visdom.http.server.services.constants.CourseAdapterConstants
import visdom.http.server.services.constants.CourseAdapterExamples
import visdom.utils.CommonConstants
import visdom.utils.WarningConstants


// scalastyle:off method.length
@SuppressWarnings(Array(WarningConstants.UnusedMethodParameter))
@Path(ServerConstants.PointsRootPath)
class PointsQueryService(pointsDataActor: ActorRef)(implicit executionContext: ExecutionContext)
extends Directives
with CourseAdapterResponseHandler
{
    val route: Route = (
        getPointsDataRoute
    )

    @GET
    @Produces(Array(MediaType.APPLICATION_JSON))
    @Operation(
        summary = CourseAdapterDescriptions.PointsQueryEndpointSummary,
        description = CourseAdapterDescriptions.PointsQueryEndpointDescription,
        parameters = Array(
            new Parameter(
                name = CourseAdapterConstants.FullName,
                in = ParameterIn.QUERY,
                required = true,
                description = CourseAdapterConstants.DescriptionFullName,
                schema = new Schema(
                    implementation = classOf[String]
                )
            ),
            new Parameter(
                name = CourseAdapterConstants.CourseId,
                in = ParameterIn.QUERY,
                required = true,
                description = CourseAdapterConstants.DescriptionCourseId,
                schema = new Schema(
                    implementation = classOf[String]
                )
            ),
            new Parameter(
                name = CourseAdapterConstants.ExerciseId,
                in = ParameterIn.QUERY,
                required = true,
                description = CourseAdapterConstants.DescriptionExerciseId,
                schema = new Schema(
                    implementation = classOf[String]
                )
            )
        ),
        responses = Array(
            new ApiResponse(
                responseCode = HttpConstants.StatusOkCode,
                description = CourseAdapterDescriptions.PointsStatusOkDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[JsObject]),
                        examples = Array(
                            new ExampleObject(
                                name = CourseAdapterExamples.ResponseExampleOkName,
                                value = CourseAdapterExamples.PointsResponseExampleOk
                            )
                        )
                    )
                )
            ),
            new ApiResponse(
                responseCode = HttpConstants.StatusInvalidCode,
                description = CourseAdapterDescriptions.StatusInvalidDescription,
                content = Array(
                    new Content(
                        schema = new Schema(implementation = classOf[ResponseProblem]),
                        examples = Array(
                            new ExampleObject(
                                name = CourseAdapterExamples.ResponseExampleInvalidName,
                                value = CourseAdapterExamples.ResponseExampleInvalid
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
    def getPointsDataRoute: RequestContext => Future[RouteResult] = (
        path(ServerConstants.PointsPath) &
        parameters(
            CourseAdapterConstants.FullName.withDefault(CommonConstants.EmptyString),
            CourseAdapterConstants.CourseId.withDefault(CommonConstants.EmptyString),
            CourseAdapterConstants.ExerciseId.withDefault(CommonConstants.EmptyString)
        )
    ) {
        (
            fullName,
            courseId,
            exerciseId
        ) => get {
            getRoute(
                pointsDataActor,
                CommitQueryInput(
                    fullName = fullName,
                    courseId = courseId,
                    exerciseId = exerciseId
                )
            )
        }
    }
}
// scalastyle:on method.length
