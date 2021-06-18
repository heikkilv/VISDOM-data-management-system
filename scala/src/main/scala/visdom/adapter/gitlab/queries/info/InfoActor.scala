package visdom.adapter.gitlab.queries.info

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.ExecutionContext
import visdom.adapter.gitlab.GitlabConstants
import visdom.adapter.gitlab.Adapter
import scala.concurrent.Future
import spray.json.JsObject
import visdom.adapter.gitlab.CommitQuery
import visdom.adapter.gitlab.TimestampQuery
import visdom.adapter.gitlab.queries.Constants
import visdom.http.server.BaseOptions
import visdom.http.server.response.GitlabAdapterInfoResponse


class InfoActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    def receive: Receive = {
        case BaseOptions => {
            log.info("Received info query")
            val response: GitlabAdapterInfoResponse = GitlabAdapterInfoResponse(
                componentType = GitlabConstants.ComponentType,
                componentName = Adapter.AdapterName,
                adapterType = GitlabConstants.AdapterType,
                version = GitlabConstants.AdapterVersion,
                startTime = Adapter.StartTime,
                apiAddress = Adapter.ApiAddress,
                swaggerDefinition = Constants.SwaggerLocation
            )

            sender() ! response
        }
    }
}
