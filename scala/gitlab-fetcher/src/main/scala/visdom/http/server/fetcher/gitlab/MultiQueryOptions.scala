package visdom.http.server.fetcher.gitlab

import spray.json.JsObject
import spray.json.JsValue
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.json.JsonUtils


final case class MultiQueryOptions(
    projectNames: String,
    filePath: Option[String],
    recursive: String,
    startDate: Option[String],
    endDate: Option[String],
    useAnonymization: String
) extends QueryOptionsBase {
    def toMap(): Map[String, JsValue] = {
        Map(
            AttributeConstants.ProjectNames -> JsonUtils.toJsonValue(projectNames),
            AttributeConstants.FilePath -> JsonUtils.toJsonValue(filePath),
            AttributeConstants.Recursive -> JsonUtils.toJsonValue(recursive),
            AttributeConstants.StartDate -> JsonUtils.toJsonValue(startDate),
            AttributeConstants.EndDate -> JsonUtils.toJsonValue(endDate),
            AttributeConstants.UseAnonymization -> JsonUtils.toJsonValue(useAnonymization)
        )
    }
    def toJsObject(): JsObject = {
        JsObject(toMap())
    }

    def toJsObject(projects: Projects): JsObject = {
        JsObject(toMap() ++ Map(AttributeConstants.Projects -> projects.toJsObject()))
    }
}
