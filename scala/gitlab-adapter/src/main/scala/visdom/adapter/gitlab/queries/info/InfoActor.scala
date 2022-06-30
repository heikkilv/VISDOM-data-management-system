// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
import visdom.adapter.gitlab.queries.Constants.SwaggerLocation
import visdom.http.server.QueryOptionsBaseObject
import visdom.http.server.response.GitlabAdapterInfoResponse
import visdom.spark.Constants


class InfoActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    def receive: Receive = {
        case QueryOptionsBaseObject => {
            log.info("Received info query")
            val response: GitlabAdapterInfoResponse = GitlabAdapterInfoResponse(
                componentType = GitlabConstants.ComponentType,
                componentName = Adapter.AdapterName,
                adapterType = GitlabConstants.AdapterType,
                version = GitlabConstants.AdapterVersion,
                database = Constants.DefaultDatabaseName,
                startTime = Adapter.StartTime,
                apiAddress = Adapter.ApiAddress,
                swaggerDefinition = SwaggerLocation
            )

            sender() ! response
        }
    }
}
