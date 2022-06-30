// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import spray.json.JsObject
import spray.json.JsString
import visdom.adapters.general.AdapterValues
import visdom.adapters.general.usecases.SingleQuery
import visdom.adapters.queries.IncludesQueryCode
import visdom.adapters.results.BaseResultValue
import visdom.http.HttpConstants
import visdom.http.server.ResponseUtils
import visdom.http.server.options.SingleOptions
import visdom.http.server.response.BaseResponse
import visdom.http.server.response.JsonResponse
import visdom.utils.QueryUtils
import visdom.utils.WartRemoverConstants


class SingleActor extends Actor with ActorLogging {
    val singleQueryClass: Class[_ <: SingleQuery] = classOf[SingleQuery]
    val singleQueryObject: IncludesQueryCode = SingleQuery
    val queryUtils: QueryUtils = AdapterValues.queryUtils

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case inputOptions: SingleOptions => {
            log.info(s"Received single query with options: ${inputOptions.toString()}")

            val response: BaseResponse = {
                (
                    queryUtils.runCacheResultQuery(
                        singleQueryObject.queryCode,
                        singleQueryClass,
                        inputOptions.toQueryOptions(),
                        10 * HttpConstants.DefaultWaitDurationSeconds
                    )
                ) match {
                    case Right(result: BaseResultValue) => result.toJsValue() match {
                        case jsObject: JsObject => JsonResponse(jsObject)
                        case message: JsString => ResponseUtils.getNotFoundResponse(message.value)
                        case _ => ResponseUtils.getNotFoundResponse(
                            s"Did not find data for id `${inputOptions.uuid}' and type '${inputOptions.objectType}'."
                        )
                    }
                    case Left(errorValue: String) => ResponseUtils.getErrorResponse(errorValue)
                }
            }

            sender() ! response
        }
    }
}
