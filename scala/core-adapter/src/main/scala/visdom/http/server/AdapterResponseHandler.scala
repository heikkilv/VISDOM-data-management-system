// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.StandardRoute
import scala.concurrent.duration.Duration


trait AdapterResponseHandler extends ResponseHandler with AdapterServerProtocol {
    override val DefaultMaxResponseDelay: Duration = 10 * ServerConstants.DefaultMaxResponseDelay

    def handleOtherResponses(receivedResponse: response.BaseResponse): StandardRoute = {
        receivedResponse match {
            case infoResponse: response.InfoResponse =>
                Directives.complete(StatusCodes.OK, infoResponse)
        }
    }
}
