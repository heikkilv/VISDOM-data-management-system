package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import spray.json.JsObject
import visdom.adapters.general.AdapterValues
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
    val cacheUpdaterClass: Class[_ <: CacheUpdater] = classOf[CacheUpdater]
    val queryUtils: QueryUtils = AdapterValues.queryUtils

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case QueryOptionsBaseObject => {
            log.info("Received update query")

            queryUtils.runSparkQuery(
                queryType = cacheUpdaterClass,
                queryOptions = CacheQueryOptions(ObjectTypes.TargetTypeAll),
                timeoutSeconds = Duration(2, TimeUnit.HOURS).toSeconds
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
