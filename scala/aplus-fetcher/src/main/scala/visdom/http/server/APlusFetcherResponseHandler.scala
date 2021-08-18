package visdom.http.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.StandardRoute


trait APlusFetcherResponseHandler extends ResponseHandler with APlusFetcherServerProtocol {
    def handleOtherResponses(receivedResponse: response.BaseResponse): StandardRoute = {
        receivedResponse match {
            case aPlusFetcherInfoResponse: response.APlusFetcherInfoResponse =>
                Directives.complete(StatusCodes.OK, aPlusFetcherInfoResponse)
        }
    }
}
