package visdom.http.server.response

import spray.json.JsObject
import visdom.json.JsonUtils
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.utils.SnakeCaseConstants


final case class APlusFetcherInformation(
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

object APlusFetcherInformation {
    def requiredKeys: Set[String] = Set(
        SnakeCaseConstants.SourceServer,
        SnakeCaseConstants.Database
    )
}
