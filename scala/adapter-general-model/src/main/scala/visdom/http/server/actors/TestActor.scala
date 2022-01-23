package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import visdom.adapters.general.usecases.TestQuery
import visdom.adapters.results.Result
import visdom.http.HttpConstants
import visdom.http.server.ResponseUtils
import visdom.http.server.options.PageWithTokenOptions
import visdom.http.server.response.BaseResponse
import visdom.http.server.response.JsonResponse
import visdom.utils.QueryUtils
import visdom.utils.WartRemoverConstants


class TestActor extends Actor with ActorLogging {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case inputOptions: PageWithTokenOptions => {
            log.info(s"Received test query with options: ${inputOptions.toString()}")

            val response: BaseResponse = {
                QueryUtils.runQuery(
                    TestQuery.queryCode,
                    classOf[TestQuery],
                    inputOptions.toQueryOptions(),
                    10 * HttpConstants.DefaultWaitDurationSeconds
                ) match {
                    case Right(result: Result) => JsonResponse(result.toJsObject())
                    case Left(errorValue: String) => ResponseUtils.getErrorResponse(errorValue)
                }
            }

            sender() ! response
        }
    }
}
