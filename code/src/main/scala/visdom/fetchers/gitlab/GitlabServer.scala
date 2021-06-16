package visdom.fetchers.gitlab

import scalaj.http.Http
import scalaj.http.HttpOptions
import scalaj.http.HttpRequest


class GitlabServer(hostAddress: String, apiToken: Option[String], allowUnsafeSSL: Option[Boolean]) {
    val hostName: String = hostAddress
    val baseAddress: String = List(hostAddress, GitlabConstants.PathBase).mkString("/")

    def modifyRequest(request: HttpRequest): HttpRequest = {
        val requestWithToken: HttpRequest = apiToken match {
            case Some(token: String) => request.header(GitlabConstants.HeaderPrivateToken, token)
            case None => request
        }

        allowUnsafeSSL match {
            case Some(allowUnsafe: Boolean) if allowUnsafe => {
                requestWithToken.option(HttpOptions.allowUnsafeSSL)
            }
            case _ => requestWithToken
        }
    }
}
