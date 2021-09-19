package visdom.http.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.StandardRoute
import scala.concurrent.duration.Duration


trait CourseAdapterResponseHandler extends ResponseHandler with CourseAdapterServerProtocol {
    override val DefaultMaxResponseDelay: Duration = 10 * ServerConstants.DefaultMaxResponseDelay

    def handleOtherResponses(receivedResponse: response.BaseResponse): StandardRoute = {
        receivedResponse match {
            case courseAdapterInfoResponse: response.CourseAdapterInfoResponse =>
                Directives.complete(StatusCodes.OK, courseAdapterInfoResponse)
        }
    }
}
