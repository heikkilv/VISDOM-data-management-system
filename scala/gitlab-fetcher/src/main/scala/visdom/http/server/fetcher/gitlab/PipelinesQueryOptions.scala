package visdom.http.server.fetcher.gitlab

import spray.json.JsObject
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.json.JsonUtils


final case class PipelinesQueryOptions(
    projectName: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.ProjectName -> JsonUtils.toJsonValue(projectName)
            )
        )
    }
}
