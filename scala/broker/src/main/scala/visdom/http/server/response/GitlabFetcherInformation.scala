// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
