package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import spray.json.JsObject
import spray.json.JsString
import visdom.adapters.general.usecases.MultiQuery
import visdom.adapters.results.BaseResultValue
import visdom.http.HttpConstants
import visdom.http.server.ResponseUtils
import visdom.http.server.options.MultiOptions
import visdom.http.server.response.BaseResponse
import visdom.http.server.response.JsonResponse
import visdom.utils.QueryUtils
import visdom.utils.WartRemoverConstants


class MultiActor extends Actor with ActorLogging {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case inputOptions: MultiOptions => {
            println(s"Received multi query with options: ${inputOptions.toString()}")

            val response: BaseResponse = {
                (
                    QueryUtils.runCacheResultQuery(
                        MultiQuery.queryCode,
                        classOf[MultiQuery],
                        inputOptions.toQueryOptions(),
                        10 * HttpConstants.DefaultWaitDurationSeconds
                    )
                ) match {
                    case Right(result: BaseResultValue) => result.toJsValue() match {
                        case jsObject: JsObject => JsonResponse(jsObject)
                        case message: JsString => ResponseUtils.getNotFoundResponse(message.value)
                        case _ => ResponseUtils.getNotFoundResponse("No results found")
                    }
                    case Left(errorValue: String) => ResponseUtils.getErrorResponse(errorValue)
                }
            }

            sender() ! response
        }
    }
}
