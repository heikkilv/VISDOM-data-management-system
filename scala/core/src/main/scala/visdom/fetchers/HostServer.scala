package visdom.fetchers

import scalaj.http.HttpRequest


abstract class HostServer(hostAddress: String, apiToken: Option[String], allowUnsafeSSL: Option[Boolean]) {
    val hostName: String = hostAddress
    val baseAddress: String

    def modifyRequest(request: HttpRequest): HttpRequest
}
