// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import java.util.concurrent.TimeoutException
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import visdom.adapters.course.options.UsernameQueryInput
import visdom.adapters.course.options.UsernameQueryOptions
import visdom.adapters.course.usecases.UsernameQuery
import visdom.http.HttpConstants
import visdom.http.server.ResponseUtils
import visdom.http.server.response.BaseResponse
import visdom.http.server.response.JsonResponse
import visdom.http.server.services.constants.CourseAdapterDescriptions
import visdom.utils.WartRemoverConstants


class UsernameQueryActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: UsernameQueryInput => {
            log.info(s"Received username query with options: ${queryOptions.toString()}")

            val response: BaseResponse = queryOptions.toUsernameQueryOptions() match {
                case Some(usernameQueryOptions: UsernameQueryOptions) => {
                    val sparkResponse: BaseResponse = try {
                        Await.result(
                            Future(
                                JsonResponse(
                                    (new UsernameQuery(usernameQueryOptions)).getResults()
                                )
                            ),
                            10 * HttpConstants.DefaultWaitDuration
                        )
                    } catch  {
                        case error: TimeoutException => ResponseUtils.getErrorResponse(error.getMessage())
                    }

                    // check if the response from Spark is empty
                    sparkResponse match {
                        case okResponse: JsonResponse => okResponse.data.fields.isEmpty match {
                            case true => ResponseUtils.getNotFoundResponse(HttpConstants.ResponseDefaultNotFound)
                            case false => sparkResponse
                        }
                        case _ => sparkResponse
                    }
                }
                case None => ResponseUtils.getInvalidResponse(CourseAdapterDescriptions.DefaultErrorDescription)
            }

            sender() ! response
        }
    }
}
