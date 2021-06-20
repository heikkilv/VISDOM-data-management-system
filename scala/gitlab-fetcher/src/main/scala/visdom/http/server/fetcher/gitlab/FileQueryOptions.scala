package visdom.http.server.fetcher.gitlab

import spray.json.JsObject
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.json.JsonUtils


final case class FileQueryOptions(
    projectName: String,
    reference: String,
    filePath: Option[String],
    recursive: String,
    includeCommitLinks: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.ProjectName -> JsonUtils.toJsonValue(projectName),
                AttributeConstants.Reference -> JsonUtils.toJsonValue(reference),
                AttributeConstants.FilePath -> JsonUtils.toJsonValue(filePath),
                AttributeConstants.Recursive -> JsonUtils.toJsonValue(recursive),
                AttributeConstants.IncludeCommitLinks -> JsonUtils.toJsonValue(includeCommitLinks)
            )
        )
    }
}
