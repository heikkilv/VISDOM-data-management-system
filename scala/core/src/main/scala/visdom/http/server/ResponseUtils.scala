package visdom.http.server

import spray.json.JsObject


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
