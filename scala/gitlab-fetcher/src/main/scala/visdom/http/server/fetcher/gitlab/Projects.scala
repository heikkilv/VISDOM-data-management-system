// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.fetcher.gitlab

import spray.json.JsObject
import visdom.http.server.AttributeConstants
import visdom.http.server.QueryOptionsBase
import visdom.json.JsonUtils


final case class Projects(
    allowed: Seq[String],
    unauthorized: Seq[String],
    notFound: Seq[String]
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                AttributeConstants.Allowed -> JsonUtils.toJsonValue(allowed),
                AttributeConstants.Unauthorized -> JsonUtils.toJsonValue(unauthorized),
                AttributeConstants.NotFound -> JsonUtils.toJsonValue(notFound)
            )
        )
    }
}
