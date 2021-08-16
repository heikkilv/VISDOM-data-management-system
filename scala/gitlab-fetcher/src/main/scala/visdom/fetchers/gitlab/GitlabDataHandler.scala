package visdom.fetchers.gitlab

import java.util.concurrent.TimeoutException
import org.mongodb.scala.bson.Document
import scala.concurrent.Await
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.fetchers.DataHandler
import visdom.http.HttpConstants
import visdom.utils.GeneralUtils


abstract class GitlabDataHandler(options: GitlabFetchOptions)
extends DataHandler(options) {
    def handleRequests(firstRequest: HttpRequest): Option[Array[Document]] = {
        def handleRequestInternal(
            requestInternal: HttpRequest,
            resultDocuments: Array[Document],
            page: Int
        ): Array[Document] = {
            def responseHelper(response: HttpResponse[String]): Array[Document] = {
                response.code match {
                    case HttpConstants.StatusCodeOk => {
                        val resultArray: Array[Document] = processResponse(response)
                        val allResults: Array[Document] = resultDocuments ++ resultArray
                        getNextRequest(requestInternal, response, page) match {
                            case Some(nextRequest: HttpRequest) =>
                                handleRequestInternal(nextRequest, allResults, page + 1)
                            case None => allResults
                        }
                    }
                    case _ => resultDocuments
                }
            }

            try {
                Await.result(
                    visdom.http.HttpUtils.makeRequest(requestInternal),
                    GitlabConstants.DefaultWaitDuration
                ) match {
                    case Some(response: HttpResponse[String]) => responseHelper(response)
                    case None => resultDocuments
                }
            } catch  {
                case _: TimeoutException => resultDocuments
            }
        }

        val requestWithPageParams: HttpRequest = firstRequest.params(
            (GitlabConstants.ParamPerPage, GitlabConstants.DefaultPerPage.toString()),
            (GitlabConstants.ParamPage, GitlabConstants.DefaultStartPage.toString())
        )
        val results: Array[Document] = handleRequestInternal(
            requestWithPageParams,
            Array(),
            GitlabConstants.DefaultStartPage
        )
        handleResults(results)
    }

    override def getProjectName(): Option[String] = {
        options match {
            case GitlabCommitOptions(_, _, projectName, _, _, _, _, _, _, _, useAnonymization) =>
                Some(GeneralUtils.getHash(projectName, useAnonymization))
            case GitlabFileOptions(_, _, projectName, _, _, _, _, useAnonymization) =>
                Some(GeneralUtils.getHash(projectName, useAnonymization))
            case GitlabPipelinesOptions(_, _, projectName, _, _, _, _, _, useAnonymization) =>
                Some(GeneralUtils.getHash(projectName, useAnonymization))
            case GitlabCommitLinkOptions(_, _, projectName, _) =>
                Some(projectName)
            case _ => None
        }
    }

    private def getNextRequest(
        request: HttpRequest,
        response: HttpResponse[String],
        currentPage: Int
    ): Option[HttpRequest] = {
        response.header(GitlabConstants.HeaderNextPage) match {
            case Some(nextPageValue: String) => visdom.utils.GeneralUtils.toInt(nextPageValue) match {
                case Some(nextPage: Int) if (nextPage == currentPage + 1) => {
                    val nextRequest: HttpRequest = visdom.http.HttpUtils.replaceRequestParam(
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
