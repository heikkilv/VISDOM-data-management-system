// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
