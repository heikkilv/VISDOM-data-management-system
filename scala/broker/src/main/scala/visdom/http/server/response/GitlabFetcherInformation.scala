package visdom.http.server.response

import spray.json.JsObject
import visdom.json.JsonUtils
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.utils.SnakeCaseConstants


final case class GitlabFetcherInformation(
    gitlabServer: String,
    database: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.GitlabServer -> JsonUtils.toJsonValue(gitlabServer),
                AttributeConstants.Database -> JsonUtils.toJsonValue(database)
            )
        )
    }
}

object GitlabFetcherInformation {
    def requiredKeys: Set[String] = Set(
        SnakeCaseConstants.GitlabServer,
        SnakeCaseConstants.Database
    )
}
