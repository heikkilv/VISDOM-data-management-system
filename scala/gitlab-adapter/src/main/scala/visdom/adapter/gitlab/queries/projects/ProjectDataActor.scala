// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapter.gitlab.queries.projects

import akka.actor.Actor
import akka.actor.ActorLogging
import java.util.concurrent.TimeoutException
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import visdom.adapter.gitlab.ProjectQuery
import visdom.adapter.gitlab.queries.Constants
import visdom.http.server.response.BaseResponse
import visdom.http.server.response.JsonResponse
import visdom.http.server.QueryOptionsBase
import visdom.http.server.ResponseUtils
import visdom.spark.Session
import visdom.utils.WartRemoverConstants


class ProjectDataActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: QueryOptionsBase => {
            log.info(s"Received projects data query with options: ${queryOptions}")

            val response: BaseResponse = {
                // get the response for the query using Spark
                val sparkResponse: BaseResponse = try {
                    Await.result(
                        Future(
                            JsonResponse(ProjectQuery.getResult(Session.getSparkSession()))
                        ),
                        Constants.DefaultWaitDuration
                    )
                } catch  {
                    case error: TimeoutException => ResponseUtils.getErrorResponse(error.getMessage())
                }

                // check if the response from Spark is empty
                sparkResponse match {
                    case okResponse: JsonResponse => okResponse.data.fields.isEmpty match {
                        case true => ResponseUtils.getNotFoundResponse(Constants.ResponseDefaultNotFound)
                        case false => sparkResponse
                    }
                    case _ => sparkResponse
                }
            }

            sender() ! response
        }
    }
}
