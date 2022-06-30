// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters

import java.time.Instant
import org.mongodb.scala.bson.BsonDocument
import visdom.adapters.options.BaseQueryOptions
import visdom.adapters.results.Result
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class QueryResultWithMetadata(
    data: Result,
    timestamp: Instant,
    queryCode: Int,
    queryOptions: BaseQueryOptions
) {
    def toBsonObject(): BsonDocument = {
        data
            .toBsonDocument()
            .append(SnakeCaseConstants.QueryCode, JsonUtils.toBsonValue(queryCode))
            .append(SnakeCaseConstants.QueryOptions, queryOptions.toBsonDocument())
            .append(SnakeCaseConstants.Timestamp, JsonUtils.toBsonValue(timestamp))
    }
}
