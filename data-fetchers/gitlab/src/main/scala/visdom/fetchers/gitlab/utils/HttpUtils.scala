package visdom.fetchers.gitlab.utils

import scalaj.http.Http
import scalaj.http.HttpOptions
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse


object HttpUtils {
    def makeRequest(inputRequest: HttpRequest): Option[HttpResponse[String]] = {
        try {
            Some(inputRequest.asString)
        }
        catch {
            case ioException: java.io.IOException => {
                println(ioException)
                None
            }
        }
    }

    def replaceRequestParam(request: HttpRequest, paramName: String, paramValue: String): HttpRequest = {
        val cleanedParams: Seq[(String, String)] = request.params.filter(param => param._1 != paramName)

        Http(request.url)
            .headers(request.headers)
            .params(cleanedParams ++ Seq((paramName, paramValue)))
            .options(request.options)
    }
}
