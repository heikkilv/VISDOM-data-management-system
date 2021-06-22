package visdom.http.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.StandardRoute
import _root_.visdom.http.server.visdom.http.server.ResponseHandler


trait GitlabAdapterResponseHandler extends ResponseHandler with GitlabAdapterServerProtocol {
    def handleOtherResponses(receivedResponse: response.BaseResponse): StandardRoute = {
        receivedResponse match {
            case gitlabAdapterInfoResponse: response.GitlabAdapterInfoResponse =>
                Directives.complete(StatusCodes.OK, gitlabAdapterInfoResponse)
        }
    }
}
