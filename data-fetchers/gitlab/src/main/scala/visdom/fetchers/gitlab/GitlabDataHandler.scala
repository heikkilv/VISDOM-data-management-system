package visdom.fetchers.gitlab

import io.circe.Json
import io.circe.JsonObject
import io.circe.parser
import io.circe.ParsingFailure
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse


abstract class GitlabDataHandler() {
    def getRequest(): HttpRequest

    def processResponse(response: HttpResponse[String]): Either[String, Vector[JsonObject]] = {
        parser.parse(response.body) match {
            case Right(jsonResult: Json) => jsonResult.asArray match {
                case Some(jsonVector) => Right(utils.JsonUtils.onlyJsonObjects(jsonVector))
                case None => Left(GitlabConstants.ErrorJsonArray)
            }
            case Left(errorValue: ParsingFailure) => Left(errorValue.message)
        }
    }

    def processAllResponses(responses: Vector[HttpResponse[String]]): Either[String, Vector[JsonObject]] = {
        def processAllResponsesInternal(
            responsesInternal: Vector[HttpResponse[String]],
            results: Vector[JsonObject]
        ): Either[String, Vector[JsonObject]] = responsesInternal.headOption match {
            case Some(response: HttpResponse[String]) => processResponse(response) match {
                case Right(jsonVector: Vector[JsonObject]) => {
                    processAllResponsesInternal(responsesInternal.drop(1), results ++ jsonVector)
                }
                case Left(errorString: String) => Left(errorString)
            }
            case None => Right(results)
        }

        processAllResponsesInternal(responses, Vector())
    }

    def makeRequests(request: HttpRequest): Vector[HttpResponse[String]] = {
        def makeRequestInternal(
            requestInternal: HttpRequest,
            responses: Vector[HttpResponse[String]],
            page: Int
        ): Vector[HttpResponse[String]] = {
            utils.HttpUtils.makeRequest(requestInternal) match {
                case Some(response: HttpResponse[String]) => response.code match {
                    case GitlabConstants.StatusCodeOk => {
                        val allResponses: Vector[HttpResponse[String]] = responses ++ Vector(response)
                        getNextRequest(requestInternal, response, page) match {
                            case Some(nextRequest: HttpRequest) => {
                                makeRequestInternal(nextRequest, allResponses, page + 1)
                            }
                            case None => allResponses
                        }
                    }
                    case _ => responses
                }
                case None => responses
            }
        }

        val requestWithPageParams: HttpRequest = request.params(
            (GitlabConstants.ParamPerPage, GitlabConstants.DefaultPerPage.toString()),
            (GitlabConstants.ParamPage, GitlabConstants.DefaultStartPage.toString())
        )
        makeRequestInternal(requestWithPageParams, Vector(), GitlabConstants.DefaultStartPage)
    }

    private def getNextRequest(
        request: HttpRequest,
        response: HttpResponse[String],
        currentPage: Int
    ): Option[HttpRequest] = {
        response.header(GitlabConstants.HeaderNextPage) match {
            case Some(nextPageValue: String) => utils.GeneralUtils.toInt(nextPageValue) match {
                case Some(nextPage: Int) if (nextPage == currentPage + 1) => {
                    val nextRequest: HttpRequest = utils.HttpUtils.replaceRequestParam(
                        request,
                        GitlabConstants.ParamPage,
                        nextPageValue
                    )
                    Some(nextRequest)
                }
                case _ => None
            }
            case None => None
        }
    }
}
