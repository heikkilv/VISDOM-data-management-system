package visdom.http.server.fetcher.gitlab

import spray.json.JsObject
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.json.JsonUtils


final case class EventQueryOptions(
    userId: String,
    actionType: Option[String],
    targetType: Option[String],
    dateAfter: Option[String],
    dateBefore: Option[String],
    useAnonymization: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.UserId -> JsonUtils.toJsonValue(userId),
                AttributeConstants.ActionType -> JsonUtils.toJsonValue(actionType),
                AttributeConstants.TargetType -> JsonUtils.toJsonValue(targetType),
                AttributeConstants.DateAfter -> JsonUtils.toJsonValue(dateAfter),
                AttributeConstants.DateBefore -> JsonUtils.toJsonValue(dateBefore),
                AttributeConstants.UseAnonymization -> JsonUtils.toJsonValue(useAnonymization)
            )
        )
    }
}
