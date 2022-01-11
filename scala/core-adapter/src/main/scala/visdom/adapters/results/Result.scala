package visdom.adapters.results

import org.mongodb.scala.bson.BsonDocument
import spray.json.JsObject
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class Result(
    counts: ResultCounts,
    results: BaseResultValue
) {
    def toBsonDocument(): BsonDocument = {
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
}
