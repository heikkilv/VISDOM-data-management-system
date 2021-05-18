package visdom.fetchers.gitlab

import javax.net.ssl.SSLParameters
import javax.xml.ws.Response

object Main extends App
{
    import io.circe.{Json, ParsingFailure}
    import scalaj.http.{Http, HttpOptions, HttpRequest, HttpResponse}

    val SmallSleep: Int = 100
    val LargeSleep: Int = 500

    def makeRequest(inputRequest: HttpRequest): Option[HttpResponse[String]] = {
        try {
            Some(inputRequest.asString)
        }
        catch {
            case e: java.io.IOException => None
        }
    }

    val request: HttpRequest = Http("https://gitlab.com/api/v4/projects")
    val responseOption: Option[HttpResponse[String]] = makeRequest(request)

    responseOption match {
        case None => println("Error occurred: received None")
        case Some(response) => {
            val decoder_result: Either[ParsingFailure, Json] = io.circe.parser.parse(response.body)
            val json_result: Json = decoder_result match {
                case Right(json_value) => json_value
                case Left(error_value) => Json.fromString(error_value.message)
            }

            val projects: Int = json_result.asArray match {
                case Some(jsonVector) => jsonVector.size
                case None => 0
            }
            println(s"Status code: ${response.code}")
            println(s"$projects projects received")
            println(s"Content: $json_result")
        }
    }
}
