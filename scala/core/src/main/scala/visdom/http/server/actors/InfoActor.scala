package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.ExecutionContext
import visdom.database.mongodb.MongoConnection
import visdom.http.server.QueryOptionsBaseObject
import visdom.http.server.ServerConstants
import visdom.http.server.response.ComponentInfoResponse
import visdom.utils.WartRemoverConstants


trait InfoActor extends Actor with ActorLogging {
    def getInfoResponse(): ComponentInfoResponse

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case QueryOptionsBaseObject => {
            log.info(ServerConstants.DefaultInfoLogText)
            val response: ComponentInfoResponse = getInfoResponse()
            sender() ! response
        }
    }
}
