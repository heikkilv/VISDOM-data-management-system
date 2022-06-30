// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.results

import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.BsonValue
import spray.json.JsArray
import spray.json.JsValue
import visdom.json.JsonUtils



final case class MultiResult(
    results: Seq[JsValue]
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonArray.fromIterable(results.map(result => JsonUtils.toBsonValue(result)))
    }

    def toJsValue(): JsValue = {
        JsArray(results:_*)
    }
}
