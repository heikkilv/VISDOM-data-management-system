package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.ExecutionContext
import visdom.database.mongodb.MongoConnection
import visdom.fetchers.gitlab.GitlabConstants
import visdom.fetchers.gitlab.GitlabFetcher
import visdom.fetchers.gitlab.Routes
import visdom.http.server.BaseOptions
import visdom.http.server.response.GitlabFetcherInfoResponse
import visdom.utils.WartRemoverConstants
import visdom.http.server.response.ComponentInfoResponse
import visdom.http.server.ServerConstants


trait InfoActor extends Actor with ActorLogging {
    def getInfoResponse(): ComponentInfoResponse

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case BaseOptions => {
            log.info(ServerConstants.DefaultInfoLogText)
            val response: ComponentInfoResponse = getInfoResponse()
            sender() ! response
        }
    }
}
