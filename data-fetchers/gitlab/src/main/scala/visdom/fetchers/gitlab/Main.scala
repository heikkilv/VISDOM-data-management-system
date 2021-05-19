package visdom.fetchers.gitlab

import io.circe.JsonObject

object Main extends App
{
    import io.circe.{Json, ParsingFailure}
    import scalaj.http.{Http, HttpOptions, HttpRequest, HttpResponse}

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

            val (projects, first_project): (Int, String) = json_result.asArray match {
                case Some(jsonVector) => jsonVector.headOption match {
                    case Some(firstJson) => firstJson.asObject match {
                        case Some(firstObject) => firstObject.apply(GitlabConstants.AttributePathWithNamespace) match {
                            case Some(pathValue) => pathValue.asString match {
                                case Some(pathString) => (jsonVector.size, pathString)
                                case None => (jsonVector.size, "")
                            }
                            case None => (jsonVector.size, "")
                        }
                        case None => (jsonVector.size, "")
                    }
                    case None => (jsonVector.size, "")
                }
                case None => (0, "")
            }
            println(s"Status code: ${response.code}")
            println(s"$projects projects received")

            val host: String = "https://gitlab.com"
            val reference: String = "master"

            val server: GitlabServer = new GitlabServer(host, None, None)
            val commitFetcherOptions: GitlabCommitOptions = GitlabCommitOptions(
                server,
                first_project,
                reference,
                None,
                None,
                None,
                Some(true),
                Some(true),
                Some(true)
            )
            val commitFetcher: GitlabCommitHandler = new GitlabCommitHandler(commitFetcherOptions)
            val commitRequest: HttpRequest = commitFetcher.getRequest()
            val responses: Vector[HttpResponse[String]] = commitFetcher.makeRequests(commitRequest)
            val commits: Either[String, Vector[JsonObject]] = commitFetcher.processAllResponses(responses)

            commits match {
                case Right(jsonResults: Vector[JsonObject]) => {
                    println(s"Found ${jsonResults.length} commits at ${first_project}")
                    jsonResults.headOption match {
                        case Some(firstCommit) => {
                            println("The first commit:")
                            println(Json.fromJsonObject(firstCommit).spaces4SortKeys)
                        }
                        case None =>
                    }
                }
                case Left(errorMessage: String) => println(s"error: ${errorMessage}")
            }

            val fileFetcherOptions: GitlabFileOptions = GitlabFileOptions(
                server,
                first_project,
                reference,
                None,
                Some(true),
                None
            )
            val fileFetcher: GitlabFileHandler = new GitlabFileHandler(fileFetcherOptions)
            val fileRequest: HttpRequest = fileFetcher.getRequest()
            val fileResponses: Vector[HttpResponse[String]] = fileFetcher.makeRequests(fileRequest)
            val files: Either[String, Vector[JsonObject]] = fileFetcher.processAllResponses(fileResponses)

            files match {
                case Right(jsonResults: Vector[JsonObject]) => {
                    println(s"Found ${jsonResults.length} files at ${first_project}")
                    jsonResults.headOption match {
                        case Some(firstFile) => {
                            println("The first file:")
                            println(Json.fromJsonObject(firstFile).spaces4SortKeys)
                        }
                        case None =>
                    }
                }
                case Left(errorMessage: String) => println(s"error: ${errorMessage}")
            }
        }
    }
}
