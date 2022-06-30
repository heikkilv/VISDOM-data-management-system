// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.options

import org.mongodb.scala.bson.BsonDocument
import visdom.utils.SnakeCaseConstants
import visdom.json.JsonUtils


final case class SingleQueryOptions(
    objectType: String,
    uuid: String
)
extends BaseQueryOptions {
    def toBsonDocument(): BsonDocument = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(objectType),
                SnakeCaseConstants.Uuid -> JsonUtils.toBsonValue(uuid)
            )
        )
    }
}
