// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.results

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.json.JsonUtils


final case class SingleResult(
    result: JsObject
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(result.fields.mapValues(value => JsonUtils.toBsonValue(value)))
    }

    def toJsValue(): JsValue = {
        result
    }
}
