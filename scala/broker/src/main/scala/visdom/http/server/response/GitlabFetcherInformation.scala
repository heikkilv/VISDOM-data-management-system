package visdom.http.server.response

import spray.json.JsObject
import visdom.json.JsonUtils
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.utils.SnakeCaseConstants


final case class GitlabFetcherInformation(
    sourceServer: String,
    database: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.SourceServer -> JsonUtils.toJsonValue(sourceServer),
                AttributeConstants.Database -> JsonUtils.toJsonValue(database)
            )
        )
    }
}

object GitlabFetcherInformation {
    def requiredKeys: Set[String] = Set(
        SnakeCaseConstants.SourceServer,
        SnakeCaseConstants.Database
    )
}
