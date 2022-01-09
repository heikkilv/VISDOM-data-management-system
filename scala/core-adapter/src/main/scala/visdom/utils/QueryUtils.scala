package visdom.utils

import java.util.concurrent.TimeoutException
import java.util.concurrent.TimeUnit
import org.apache.spark.sql.SparkSession
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import visdom.adapters.DefaultAdapterValues
import visdom.adapters.options.BaseQueryOptions
import visdom.adapters.queries.BaseQuery
import visdom.adapters.results.Result
import visdom.spark.Session


object QueryUtils {
    implicit val ec: ExecutionContext = ExecutionContext.global

    def runQuery(
        queryCode: Int,
        queryType: Class[_ <: BaseQuery],
        queryOptions: BaseQueryOptions,
        timeoutSeconds: Double
    ): Either[String, Result] = {
        val sparkSession: SparkSession = Session.getSparkSession()
        try {
            Right(
                Await.result(
                    Future(runSparkQueryUsingCache(queryCode, queryType, queryOptions)),
                    Duration(timeoutSeconds, TimeUnit.SECONDS)
                )
            )
        } catch  {
            case error: TimeoutException => Left(error.getMessage())
        }
    }

    def runSparkQueryUsingCache(
        queryCode: Int,
        queryType: Class[_ <: BaseQuery],
        queryOptions: BaseQueryOptions
    ): Result = {
        DefaultAdapterValues.cache.getResult(queryCode, queryOptions) match {
            case Some(cachedResult: Result) => {
                println(s"Using result from cache for query ${queryCode} with ${queryOptions}")
                cachedResult
            }
            case None => {
                val result: Result = runSparkQuery(queryType, queryOptions)

                DefaultAdapterValues.cache.addResult(queryCode, queryOptions, result)
                result
            }
        }
    }

    def runSparkQuery(queryType: Class[_ <: BaseQuery], queryOptions: BaseQueryOptions): Result = {
        val sparkSession: SparkSession = Session.getSparkSession()
        val result: Result =
            queryType
                .getDeclaredConstructor()
                .newInstance(queryOptions, sparkSession)
                .getResults()

        sparkSession.stop()
        result
    }
}
