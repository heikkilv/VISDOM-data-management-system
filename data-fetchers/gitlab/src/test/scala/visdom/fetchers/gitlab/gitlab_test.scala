import org.scalatest.funsuite.AnyFunSuite
import visdom.fetchers.gitlab.Main

class SetSuite extends AnyFunSuite {
    val emptyResponse = scalaj.http.HttpResponse("", 0, Map())
    val emptyJson = io.circe.Json.fromJsonObject(io.circe.JsonObject.empty)

    val responseOption = Main.makeRequest(scalaj.http.Http("https://gitlab.com/api/v4/projects"))
    val response = responseOption.getOrElse(emptyResponse)

    test("Testing API response status code from gitlab.com") {
        assert(response.code == 200)
    }

    test("Testing API response content from gitlab.com") {
        val responseJson = io.circe.parser.parse(response.body).getOrElse(emptyJson)
        val responseVector = responseJson.asArray.getOrElse(Vector[io.circe.Json]())

        // the default maximum objects in the response is 20
        assert(responseVector.size == 20)
    }
}
