package visdom.adapters.queries

import org.apache.spark.sql.SparkSession
import visdom.adapters.options.BaseQueryOptions
import visdom.adapters.results.Result


abstract class BaseSparkQuery(queryOptions: BaseQueryOptions, sparkSession: SparkSession) {
    def runQuery(): Unit = {}

    def getResults(): Result = {
        Result.getEmpty()
    }
}

object BaseSparkQuery extends IncludesQueryCode {
    val queryCode: Int = 0
}
