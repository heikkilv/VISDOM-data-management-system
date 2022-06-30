// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.results

import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.BsonValue
import spray.json.JsValue
import spray.json.JsString


final case class NoResult(
    message: String
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonString(message)
    }

    def toJsValue(): JsValue = {
        JsString(message)
    }
}
