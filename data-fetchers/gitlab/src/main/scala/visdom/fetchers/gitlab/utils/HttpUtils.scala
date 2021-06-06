package visdom.fetchers.gitlab.utils

import java.util.concurrent.TimeoutException
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpOptions
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import visdom.fetchers.gitlab.GitlabConstants
import visdom.fetchers.gitlab.GitlabServer


object HttpUtils {
    implicit val ec: ExecutionContext = ExecutionContext.global

    def makeRequest(inputRequest: HttpRequest): Future[Option[HttpResponse[String]]] = {
        Future(
            try {
                Some(inputRequest.asString)
            }
            catch {
                case ioException: java.io.IOException => {
                    println(ioException)
                    None
                }
            }
        )
    }

    def replaceRequestParam(request: HttpRequest, paramName: String, paramValue: String): HttpRequest = {
        val cleanedParams: Seq[(String, String)] = request.params.filter(param => param._1 != paramName)

        Http(request.url)
            .headers(request.headers)
            .params(cleanedParams ++ Seq((paramName, paramValue)))
            .options(request.options)
    }

    def returnRequestStatusCode(request: HttpRequest): Int = {
        try {
            Await.result(
                makeRequest(request),
                GitlabConstants.DefaultWaitDuration
            ) match {
                case Some(response: HttpResponse[String]) => response.code
                case None => GitlabConstants.StatusCodeUnknown
            }
        } catch {
             case _: TimeoutException => GitlabConstants.StatusCodeUnknown
        }
    }

    def getProjectQueryStatusCode(gitlabServer: GitlabServer, projectName: String): Int = {
        returnRequestStatusCode(
            gitlabServer.modifyRequest(
                Http(
                    List(
                        gitlabServer.baseAddress,
                        GitlabConstants.PathProjects,
                        urlEncode(projectName, utf8)
                    ).mkString("/")
                )
            )
        )
    }
}
