package visdom.http.server.fetcher.gitlab

import spray.json.JsObject
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.json.JsonUtils


final case class PipelinesQueryOptions(
    projectName: String,
    reference: String,
    startDate: Option[String],
    endDate: Option[String],
    includeJobs: String,
    includeJobLogs: String,
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.ProjectName -> JsonUtils.toJsonValue(projectName),
                AttributeConstants.Reference -> JsonUtils.toJsonValue(reference),
                AttributeConstants.StartDate -> JsonUtils.toJsonValue(startDate),
                AttributeConstants.EndDate -> JsonUtils.toJsonValue(endDate),
                AttributeConstants.IncludeJobs -> JsonUtils.toJsonValue(includeJobs),
                AttributeConstants.IncludeJobLogs -> JsonUtils.toJsonValue(includeJobLogs)
            )
        )
    }
}
