package visdom.adapters.queries

import org.apache.spark.sql.SparkSession
import visdom.adapters.options.BaseQueryOptions
import visdom.adapters.results.Result


abstract class BaseQuery(queryOptions: BaseQueryOptions, sparkSession: SparkSession) {
    def getResults(): Result
}

object BaseQuery extends IncludesQueryCode {
    val queryCode: Int = 0
}
