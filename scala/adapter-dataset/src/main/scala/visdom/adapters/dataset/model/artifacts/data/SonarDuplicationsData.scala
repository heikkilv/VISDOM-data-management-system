// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset.model.artifacts.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class SonarDuplicationsData(
    lines: Int,
    blocks: Int,
    files: Int
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Lines -> JsonUtils.toBsonValue(lines),
                SnakeCaseConstants.Blocks -> JsonUtils.toBsonValue(blocks),
                SnakeCaseConstants.Files -> JsonUtils.toBsonValue(files)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Lines -> JsonUtils.toJsonValue(lines),
                SnakeCaseConstants.Blocks -> JsonUtils.toJsonValue(blocks),
                SnakeCaseConstants.Files -> JsonUtils.toJsonValue(files)
            )
        )
    }
}
