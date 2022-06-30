// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapter.gitlab.results

import spray.json.JsNumber
import spray.json.JsValue


final case class CommitResult(
    project_name: String,
    committer_name: String,
    date: String,
    count: Long
) {
    def toJsonTuple(): (String, String, String, JsValue) = {
        (
            project_name,
            committer_name,
            date,
            JsNumber(count)
        )
    }
}
