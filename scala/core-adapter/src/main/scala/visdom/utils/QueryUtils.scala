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
            Await.result(
                Future(runSparkQueryUsingCache(queryCode, queryType, queryOptions)),
                Duration(timeoutSeconds, TimeUnit.SECONDS)
            )
        } catch  {
            case error: TimeoutException => Left(error.getMessage())
        }
    }

    def runSparkQueryUsingCache(
        queryCode: Int,
        queryType: Class[_ <: BaseQuery],
        queryOptions: BaseQueryOptions
    ): Either[String, Result] = {
        DefaultAdapterValues.cache.getResult(queryCode, queryOptions) match {
            case Some(cachedResult: Result) => {
                println(s"Using result from cache for query ${queryCode} with ${queryOptions}")
                Right(cachedResult)
            }
            case None => {
                val result: Either[String, Result] = runSparkQuery(queryType, queryOptions)

                result match {
                    case Right(resultValue: Result) =>
                        DefaultAdapterValues.cache.addResult(queryCode, queryOptions, resultValue)
                    case _ =>
                }

                result
            }
        }
    }

    def runSparkQuery[QueryOptions <: BaseQueryOptions](
        queryType: Class[_ <: BaseQuery],
        queryOptions: QueryOptions
    ): Either[String, Result] = {
        val sparkSession: SparkSession = Session.getSparkSession()
        val result: Either[String, Result] =
            try {
                Right(
                    queryType
                        .getDeclaredConstructor(queryOptions.getClass(), classOf[SparkSession])
                        .newInstance(queryOptions, sparkSession)
                        .getResults()
                )
            } catch {
                case error: java.lang.NoSuchMethodException => Left(error.toString())
            }

        sparkSession.stop()
        result
    }
}
