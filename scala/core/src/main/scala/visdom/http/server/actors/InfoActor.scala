// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
