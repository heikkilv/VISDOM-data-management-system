// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset.model.authors.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.utils.SnakeCaseConstants
import visdom.json.JsonUtils


final case class UserData(
    commits: Int,
    issues: Int
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Commits -> JsonUtils.toBsonValue(commits),
                SnakeCaseConstants.Issues -> JsonUtils.toBsonValue(issues)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Commits -> JsonUtils.toJsonValue(commits),
                SnakeCaseConstants.Issues -> JsonUtils.toJsonValue(issues)
            )
        )
    }
}
