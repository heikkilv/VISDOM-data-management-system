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
        response.ResponseAccepted(ServerConstants.QueryAcceptedStatus, description, options)
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
}
