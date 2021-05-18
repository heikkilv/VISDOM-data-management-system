package visdom.fetchers.gitlab

import io.circe.{Json, JsonObject, ParsingFailure, parser}
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
import scalaj.http.{Http, HttpRequest, HttpResponse}


class GitlabCommitHandler(
    hostAddress: String,
    projectName: String,
    reference: String,
    includeStatistics: Boolean
) extends GitlabDataHandler {

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/commits.html
        val uri: String = List(
            hostAddress,
            GitlabConstants.PathProjects,
            URLEncoder.encode(projectName, UTF_8.name()),
            GitlabConstants.PathRepository,
            GitlabConstants.PathCommits
        ).mkString("/")

        val params: Map[String, String] = Map(
            GitlabConstants.ParamRef -> reference,
            GitlabConstants.ParamWithStats -> includeStatistics.toString()
        )

        Http(uri).params(params)
    }

    def processResponse(response: HttpResponse[String]): Either[String, Vector[JsonObject]] = {
        parser.parse(response.body) match {
            case Left(errorValue: ParsingFailure) => Left(errorValue.message)
            case Right(jsonResult: Json) => jsonResult.asArray match {
                case None => Left("Invalid JSON array")
                case Some(jsonVector) => {
                    val jsonObjectVector: Vector[JsonObject] = JsonUtils.onlyJsonObjects(jsonVector)
                    val jsonObjectVectorWithProjectNames: Vector[JsonObject] = jsonObjectVector.map(
                        jsonObject => addProjectName(jsonObject)
                    )
                    Right(jsonObjectVectorWithProjectNames)
                }
            }
        }
    }

    private def addProjectName(jsonObject: JsonObject): JsonObject = {
        jsonObject.add(GitlabConstants.AttributeProjectName, Json.fromString(projectName))
    }
}
