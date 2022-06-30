// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class ModuleCommitsOutput(
    module_name: String,
    projects: Seq[ExerciseCommitsOutput]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.ModuleName -> JsonUtils.toJsonValue(module_name),
            SnakeCaseConstants.Projects -> JsonUtils.toJsonValue(projects)
        )
    }
}
