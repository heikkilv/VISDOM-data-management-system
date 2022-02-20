package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import spray.json.JsValue
import visdom.adapters.options.BaseQueryWithPageOptions
import visdom.adapters.results.BaseResultValue
import visdom.adapters.results.MultiResult
import visdom.adapters.results.Result
import visdom.adapters.results.ResultCounts
import visdom.json.JsonUtils


object AdapterUtils {
    def getTotalCount[T](dataset: Dataset[T]): Int = {
        dataset.count().toInt
    }

    def getFirstIndex(pageOptions: BaseQueryWithPageOptions): Int = {
        (pageOptions.page - 1) * pageOptions.pageSize + 1
    }

    def getFirstIndex(pageOptions: BaseQueryWithPageOptions, totalCount: Int): Int = {
        Math.max(
            Math.min(getFirstIndex(pageOptions), totalCount),
            0
        )
    }

    def getLastIndex(pageOptions: BaseQueryWithPageOptions): Int = {
        pageOptions.page * pageOptions.pageSize
    }

    def getLastIndex(pageOptions: BaseQueryWithPageOptions, totalCount: Int): Int = {
        Math.max(
            Math.min(getLastIndex(pageOptions), totalCount),
            0
        )
    }

    def getResultCount(totalCount: Int, pageOptions: BaseQueryWithPageOptions): Int = {
        Math.min(
            Math.max(
                totalCount - (pageOptions.page - 1) * pageOptions.pageSize,
                0
            ),
            pageOptions.pageSize
        )
    }

    def getResult[T <: BaseResultValue](
        dataset: Dataset[T],
        pageOptions: BaseQueryWithPageOptions,
        sortColumn: String
    ): Result = {
        val totalCount: Int = getTotalCount(dataset)
        val resultCount: Int = getResultCount(totalCount, pageOptions)

        val resultData: Array[JsValue] = resultCount > 0 match {
            case true =>
                dataset
                    .sort(sortColumn)
                    .limit(getLastIndex(pageOptions))
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

    def getResult(
        pageOptions: BaseQueryWithPageOptions,
        sortColumn: String,
        valueArrays: Array[JsValue]*
    ): Result = {
        val resultData: Array[JsValue] =
            JsonUtils.combineSortedArrays(sortColumn, valueArrays:_*)
        val totalCount: Int = resultData.length
        val resultCount: Int = getResultCount(totalCount, pageOptions)

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

    def getStrings(
        dataset: Dataset[String],
        pageOptions: BaseQueryWithPageOptions
    ): Set[String] = {
        val totalCount: Int = getTotalCount(dataset)
        val resultCount: Int = getResultCount(totalCount, pageOptions)

        resultCount > 0 match {
            case true =>
                dataset
                    .sort(dataset.columns(0))
                    .limit(getLastIndex(pageOptions))
                    .tail(resultCount)
                    .toSet
            case false => Set.empty
        }
    }
}
