package visdom.http.server.response

import spray.json.JsObject
import visdom.json.JsonUtils
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.utils.SnakeCaseConstants


final case class GitlabAdapterInformation(
    database: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.Database -> JsonUtils.toJsonValue(database)
            )
        )
    }
}

object GitlabAdapterInformation {
    def requiredKeys: Set[String] = Set(
        SnakeCaseConstants.Database
    )
}
