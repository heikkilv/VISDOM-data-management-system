package visdom.http.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.StandardRoute


trait GitlabFetcherResponseHandler extends ResponseHandler with GitlabFetcherServerProtocol {
    def handleOtherResponses(receivedResponse: response.BaseResponse): StandardRoute = {
        receivedResponse match {
            case gitlabFetcherInfoResponse: response.GitlabFetcherInfoResponse =>
                Directives.complete(StatusCodes.OK, gitlabFetcherInfoResponse)
        }
    }
}
