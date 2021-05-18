package visdom.fetchers.gitlab

import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import io.circe.JsonObject


abstract class GitlabDataHandler() {
    def getRequest(): HttpRequest
    def processResponse(response: HttpResponse[String]): Either[String, Vector[JsonObject]]
    def processAllResponses(responses: Vector[HttpResponse[String]]): Either[String, Vector[JsonObject]]
    // def makeRequest(request: HttpRequest): Vector[HttpResponse[String]]
}
