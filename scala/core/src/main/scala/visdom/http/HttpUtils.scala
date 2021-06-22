package visdom.http

import java.util.concurrent.TimeoutException
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future


object HttpUtils {
    implicit val ec: ExecutionContext = ExecutionContext.global

    def makeRequest(inputRequest: HttpRequest): Future[Option[HttpResponse[String]]] = {
        Future(
            try {
                Some(inputRequest.asString)
            }
            catch {
                case ioException: java.io.IOException => {
                    println(ioException)
                    None
                }
            }
        )
    }

    // adds or replaces the given query parameter to the GET request
    def replaceRequestParam(request: HttpRequest, paramName: String, paramValue: String): HttpRequest = {
        val cleanedParams: Seq[(String, String)] = request.params.filter(param => param._1 != paramName)

        Http(request.url)
            .charset(request.charset)
            .headers(request.headers)
            .params(cleanedParams ++ Seq((paramName, paramValue)))
            .options(request.options)
    }

    def returnRequestStatusCode(request: HttpRequest): Int = {
        try {
            Await.result(
                makeRequest(request),
                HttpConstants.DefaultWaitDuration
            ) match {
                case Some(response: HttpResponse[String]) => response.code
                case None => HttpConstants.StatusCodeUnknown
            }
        } catch {
             case _: TimeoutException => HttpConstants.StatusCodeUnknown
        }
    }
}
