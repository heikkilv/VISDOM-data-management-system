package visdom.fetchers.aplus

import java.util.concurrent.TimeoutException
import org.mongodb.scala.bson.Document
import scala.concurrent.Await
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.fetchers.DataHandler
import visdom.http.HttpConstants
import visdom.utils.GeneralUtils


abstract class APlusDataHandler(options: APlusFetchOptions)
extends DataHandler(options) {
    def handleRequests(firstRequest: HttpRequest): Option[Array[Document]] = {
        val results: Option[Array[Document]] =
            try {
                Await.result(
                    visdom.http.HttpUtils.makeRequest(firstRequest),
                    APlusConstants.DefaultWaitDuration
                ) match {
                    case Some(response: HttpResponse[String]) => response.code match {
                        case HttpConstants.StatusCodeOk => Some(processResponse(response))
                        case _ => None
                    }
                    case None => None
                }
            } catch  {
                case _: TimeoutException => None
            }

        handleResults(results match {
            case Some(resultArray: Array[Document]) => resultArray
            case None => Array[Document]()
        })
    }
}
