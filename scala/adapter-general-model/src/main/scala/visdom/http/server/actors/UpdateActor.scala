package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import spray.json.JsObject
import visdom.adapters.general.usecases.CacheUpdater
import visdom.adapters.options.CacheQueryOptions
import visdom.adapters.options.ObjectTypes
import visdom.http.HttpConstants
import visdom.http.server.QueryOptionsBaseObject
import visdom.http.server.response.JsonResponse
import visdom.http.server.services.constants.GeneralAdapterDescriptions
import visdom.json.JsonUtils
import visdom.utils.QueryUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


class UpdateActor extends Actor with ActorLogging {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case QueryOptionsBaseObject => {
            log.info("Received update query")

            QueryUtils.runSparkQuery(
                queryType = classOf[CacheUpdater],
                queryOptions = CacheQueryOptions(ObjectTypes.TargetTypeAll),
                timeoutSeconds = 100 * HttpConstants.DefaultWaitDurationSeconds
            )

            val response: JsonResponse = JsonResponse(
                JsObject(
                    Map(
                        SnakeCaseConstants.Message ->
                            JsonUtils.toJsonValue(GeneralAdapterDescriptions.UpdateOkDescription)
                    )
                )
            )
            sender() ! response
        }
    }
}
