package visdom.http.server

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.StandardRoute
import akka.pattern.ask
import java.util.concurrent.TimeoutException
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import spray.json.JsObject


trait ResponseHandler extends ServerProtocol {
    def handleOtherResponses(receivedResponse: response.BaseResponse): StandardRoute

    val DefaultMaxResponseDelay: Duration = ServerConstants.DefaultMaxResponseDelay

    def getRoute(actorReference: ActorRef, queryOptions: QueryOptionsBase): StandardRoute = {
        val receivedResponse: response.BaseResponse = try {
            Await.result(
                (actorReference ? queryOptions).mapTo[response.BaseResponse],
                DefaultMaxResponseDelay
            )
        } catch  {
            case error: TimeoutException => ResponseUtils.getErrorResponse(error.getMessage())
        }

        receivedResponse match {
            case jsonResponse: response.JsonResponse =>
                Directives.complete(StatusCodes.OK, jsonResponse.data)
            case jsonArrayResponse: response.JsonArrayResponse =>
                Directives.complete(StatusCodes.OK, jsonArrayResponse.array)
            case acceptedResponse: response.ResponseAccepted =>
                Directives.complete(StatusCodes.Accepted, acceptedResponse)
            case problemResponse: response.ResponseProblem =>
                Directives.complete(getStatusCode(problemResponse), problemResponse)
            case _ => handleOtherResponses(receivedResponse)
        }
    }

    def getStatusCode(problemResponse: response.ResponseProblem): StatusCode = {
        problemResponse.status match {
            case ServerConstants.QueryInvalidStatus => StatusCodes.BadRequest
            case ServerConstants.QueryUnauthorizedStatus => StatusCodes.Unauthorized
            case ServerConstants.QueryNotFoundStatus => StatusCodes.NotFound
            case ServerConstants.QueryErrorStatus => StatusCodes.InternalServerError
        }
    }
}
