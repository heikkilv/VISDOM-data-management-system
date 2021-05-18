package visdom.fetchers.gitlab

import scalaj.http.HttpRequest
import scalaj.http.HttpResponse


object HttpUtils {
    def makeRequest(inputRequest: HttpRequest): Option[HttpResponse[String]] = {
        try {
            Some(inputRequest.asString)
        }
        catch {
            case ioException: java.io.IOException => {
                println(ioException.getMessage())
                None
            }
        }
    }
}
