package visdom.http.server

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.StandardRoute
import akka.pattern.ask
import java.util.concurrent.TimeoutException
import scala.concurrent.Await
import spray.json.JsObject
import akka.http.scaladsl.model.StatusCode


object ResponseUtils extends ServerProtocol {
    def getAcceptedResponse[OptionsType](
        description: String,
        options: JsObject
    ): response.ResponseAccepted = {
        response.ResponseAccepted(ServerConstants.QueryInvalidStatus, description, options)
    }

    def getInvalidResponse(description: String): response.ResponseProblem = {
        response.ResponseProblem(ServerConstants.QueryInvalidStatus, description)
    }

    def getUnauthorizedResponse(description: String): response.ResponseProblem = {
        response.ResponseProblem(ServerConstants.QueryUnauthorizedStatus, description)
    }

    def getNotFoundResponse(description: String): response.ResponseProblem = {
        response.ResponseProblem(ServerConstants.QueryNotFoundStatus, description)
    }

    def getErrorResponse(description: String): response.ResponseProblem = {
        response.ResponseProblem(ServerConstants.QueryErrorStatus, description)
    }

    def getRoute(actorReference: ActorRef, queryOptions: QueryOptionsBase): StandardRoute = {
        val receivedResponse: response.BaseResponse = try {
            Await.result(
                (actorReference ? queryOptions).mapTo[response.BaseResponse],
                ServerConstants.DefaultMaxResponseDelay
            )
        } catch  {
            case error: TimeoutException => getErrorResponse(error.getMessage())
        }

        receivedResponse match {
            case jsonResponse: response.JsonResponse =>
                Directives.complete(StatusCodes.OK, jsonResponse.data)
            case acceptedResponse: response.ResponseAccepted =>
                Directives.complete(StatusCodes.Accepted, acceptedResponse)
            case gitlabFetcherInfoResponse: response.GitlabFetcherInfoResponse =>
                Directives.complete(StatusCodes.OK, gitlabFetcherInfoResponse)
            case gitlabAdapterInfoResponse: response.GitlabAdapterInfoResponse =>
                Directives.complete(StatusCodes.OK, gitlabAdapterInfoResponse)
            case problemResponse: response.ResponseProblem =>
                Directives.complete(getStatusCode(problemResponse), problemResponse)
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
