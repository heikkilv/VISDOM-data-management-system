package visdom.fetchers.gitlab

import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import io.circe.JsonObject


abstract class GitlabDataHandler() {
    def getRequest(): HttpRequest
    def processResponse(response: HttpResponse[String]): Either[String, Vector[JsonObject]]

    def processAllResponses(responses: Vector[HttpResponse[String]]): Either[String, Vector[JsonObject]] = {
        def processAllResponsesInternal(
            responsesInternal: Vector[HttpResponse[String]],
            results: Vector[JsonObject]
        ): Either[String, Vector[JsonObject]] = responsesInternal.headOption match {
            case None => Right(results)
            case Some(response: HttpResponse[String]) => processResponse(response) match {
                case Left(errorString: String) => Left(errorString)
                case Right(jsonVector: Vector[JsonObject]) => {
                    processAllResponsesInternal(responsesInternal.drop(1), results ++ jsonVector)
                }
            }
        }

        processAllResponsesInternal(responses, Vector())
    }

    def makeRequest(request: HttpRequest): Vector[HttpResponse[String]] = {
        def makeRequestInternal(
            requestInternal: HttpRequest,
            responses: Vector[HttpResponse[String]],
            page: Int
        ): Vector[HttpResponse[String]] = {
            HttpUtils.makeRequest(requestInternal) match {
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
                }
                case None => responses
            }
        }

        val requestWithPageParams: HttpRequest = request.params(
            (GitlabConstants.ParamPerPage, GitlabConstants.DefaultPerPage.toString()),
            (GitlabConstants.ParamPage, GitlabConstants.DefaultStartPage.toString())
        )
        makeRequestInternal(request, Vector(), GitlabConstants.DefaultStartPage)
    }

    private def getNextRequest(
        request: HttpRequest,
        response: HttpResponse[String],
        currentPage: Int
    ): Option[HttpRequest] = {
        response.header(GitlabConstants.HeaderNextPage) match {
            case Some(nextPageValue: String) => GeneralUtils.toInt(nextPageValue) match {
                case Some(nextPage: Int) if (nextPage == currentPage + 1) => {
                    Some(request.param(GitlabConstants.HeaderNextPage, nextPageValue))
                }
                case _ => None
            }
            case None => None
        }
    }
}
