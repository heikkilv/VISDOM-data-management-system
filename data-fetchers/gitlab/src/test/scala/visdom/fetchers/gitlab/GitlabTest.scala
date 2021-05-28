import org.scalatest.funsuite.AnyFunSuite
import visdom.fetchers.gitlab.utils.HttpUtils

class GitlabTest extends AnyFunSuite {
    val emptyResponse = scalaj.http.HttpResponse("", 0, Map())

    val responseOption = HttpUtils.makeRequest(scalaj.http.Http("https://gitlab.com/api/v4/projects"))
    val response = responseOption.getOrElse(emptyResponse)

    test("Testing API response status code from gitlab.com") {
        assert(response.code == 200)
    }

    test("Testing API response content from gitlab.com") {
        val responseJson = org.bson.BsonArray.parse(response.body)

        // the default maximum objects in the response is 20
        assert(responseJson.size == 20)
    }
}
