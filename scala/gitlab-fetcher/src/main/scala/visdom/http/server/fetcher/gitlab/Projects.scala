package visdom.http.server.fetcher.gitlab

import spray.json.JsObject
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.json.JsonUtils


final case class Projects(
    allowed: Seq[String],
    unauthorized: Seq[String],
    notFound: Seq[String]
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.Allowed -> JsonUtils.toJsonValue(allowed),
                AttributeConstants.Unauthorized -> JsonUtils.toJsonValue(unauthorized),
                AttributeConstants.NotFound -> JsonUtils.toJsonValue(notFound)
            )
        )
    }
}
