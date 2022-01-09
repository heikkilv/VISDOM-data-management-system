package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import scala.reflect.ClassTag
import spray.json.JsValue
import visdom.adapters.options.BaseQueryWithPageOptions
import visdom.adapters.results.BaseResultValue
import visdom.adapters.results.MultiResult
import visdom.adapters.results.Result
import visdom.adapters.results.ResultCounts


object AdapterUtils {
    def getResult[T <: BaseResultValue](
        dataset: Dataset[T],
        pageOptions: BaseQueryWithPageOptions,
        sortColumn: String
    ): Result = {
        val totalCount: Int = dataset.count().toInt
        val lastIndex: Int = pageOptions.page * pageOptions.pageSize
        val resultCount: Int =
            Math.min(
                Math.max(
                    totalCount - (pageOptions.page - 1) * pageOptions.pageSize,
                    0
                ),
                pageOptions.pageSize
            )

        val resultData: Array[JsValue] = resultCount > 0 match {
            case true =>
                dataset
                    .sort(sortColumn)
                    .limit(lastIndex)
                    .tail(resultCount)
                    .map(dataValue => dataValue.toJsValue())
            case false => Array.empty
        }

        Result(
            counts = ResultCounts(
                count = resultCount,
                totalCount = totalCount,
                page = pageOptions.page,
                pageSize = pageOptions.pageSize
            ),
            results = MultiResult(
                results = resultData
            )
        )
    }
}
