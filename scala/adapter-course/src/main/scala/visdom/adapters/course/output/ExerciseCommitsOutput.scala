// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ExerciseCommitsOutput(
    name: String,
    commit_count: Int,
    commit_meta: Seq[CommitOutput]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.Name -> JsonUtils.toJsonValue(name),
            SnakeCaseConstants.CommitCount -> JsonUtils.toJsonValue(commit_count),
            SnakeCaseConstants.CommitMeta -> JsonUtils.toJsonValue(commit_meta)
        )
    }
}
