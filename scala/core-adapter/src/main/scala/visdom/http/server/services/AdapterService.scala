package visdom.http.server.services

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import visdom.http.server.AdapterResponseHandler


trait AdapterService
extends Directives
with AdapterResponseHandler {
    val route: Route
}
