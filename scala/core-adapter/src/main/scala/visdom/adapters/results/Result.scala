// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.results

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.http.server.response.BaseResponse
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class Result(
    counts: ResultCounts,
    results: BaseResultValue
)
extends BaseResponse
with BaseResultValue {
    def toBsonDocument(): BsonDocument = {
        // NOTE: should return the same as the following:
        // JsonUtils.toBsonValue(toJsObject()).asDocument()

        BsonDocument(
            Map(
                SnakeCaseConstants.Count -> JsonUtils.toBsonValue(counts.count),
                SnakeCaseConstants.TotalCount -> JsonUtils.toBsonValue(counts.totalCount),
                SnakeCaseConstants.Page -> JsonUtils.toBsonValue(counts.page),
                SnakeCaseConstants.PageSize -> JsonUtils.toBsonValue(counts.pageSize),
                SnakeCaseConstants.PreviousPage -> JsonUtils.toBsonValue(counts.previousPage),
                SnakeCaseConstants.NextPage -> JsonUtils.toBsonValue(counts.nextPage),
                SnakeCaseConstants.Results -> results.toBsonValue()
            )
        )
    }

    def toBsonValue(): BsonValue = {
        toBsonDocument()
    }

    def toJsObject(): JsObject = {
        JsObject(
            Map(
                SnakeCaseConstants.Count -> JsonUtils.toJsonValue(counts.count),
                SnakeCaseConstants.TotalCount -> JsonUtils.toJsonValue(counts.totalCount),
                SnakeCaseConstants.Page -> JsonUtils.toJsonValue(counts.page),
                SnakeCaseConstants.PageSize -> JsonUtils.toJsonValue(counts.pageSize),
                SnakeCaseConstants.PreviousPage -> JsonUtils.toJsonValue(counts.previousPage),
                SnakeCaseConstants.NextPage -> JsonUtils.toJsonValue(counts.nextPage),
                SnakeCaseConstants.Results -> results.toJsValue()
            )
        )
    }

    def toJsValue(): JsValue = {
        toJsObject()
    }
}

object Result {
    def getEmpty(): Result = {
        Result(ResultCounts.getEmpty(), MultiResult(Seq.empty))
    }
}
