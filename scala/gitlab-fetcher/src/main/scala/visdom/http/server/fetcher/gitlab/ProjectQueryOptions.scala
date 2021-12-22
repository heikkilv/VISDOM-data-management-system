package visdom.http.server.fetcher.gitlab

import spray.json.JsObject
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.json.JsonUtils


final case class ProjectQueryOptions(
    projectId: Option[String],
    projectName: Option[String],
    useAnonymization: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.ProjectId -> JsonUtils.toJsonValue(projectId),
                AttributeConstants.ProjectName -> JsonUtils.toJsonValue(projectName),
                AttributeConstants.UseAnonymization -> JsonUtils.toJsonValue(useAnonymization)
            )
        )
    }
}
