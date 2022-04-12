package visdom.utils

import java.util.concurrent.TimeoutException
import java.util.concurrent.TimeUnit
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.language.existentials
import visdom.adapters.QueryCache
import visdom.adapters.options.BaseQueryOptions
import visdom.adapters.queries.BaseCacheQuery
import visdom.adapters.queries.BaseSparkQuery
import visdom.adapters.results.BaseResultValue
import visdom.adapters.results.NoResult
import visdom.adapters.results.Result


class QueryUtils(cache: QueryCache) {
    implicit val ec: ExecutionContext = ExecutionContext.global
    private val log = org.slf4j.LoggerFactory.getLogger("QueryUtils")

    implicit class EnrichedDataSet[DataSetType, FilterType](dataset: Dataset[DataSetType]) {
        def applyContainsFilter(columnName: String, valueOption: Option[FilterType]): Dataset[DataSetType] = {
            valueOption match {
                case Some(filterValue) =>
                    dataset.filter(functions.column(columnName).contains(filterValue))
                case None => dataset
            }
        }

        def applyEqualsFilter(columnName: String, valueOption: Option[FilterType]): Dataset[DataSetType] = {
            valueOption match {
                case Some(filterValue) =>
                    dataset.filter(functions.column(columnName) === filterValue)
                case None => dataset
            }
        }
    }

    def runCacheResultQuery(
        queryCode: Int,
        queryType: Class[_ <: BaseCacheQuery],
        queryOptions: BaseQueryOptions,
        timeoutSeconds: Double
    ): Either[String, BaseResultValue] = {
        try {
            Await.result(
                Future(
                    runCacheResultQueryUsingCache(queryCode, queryType, queryOptions) match {
                        case Some(resultValue: BaseResultValue) => Right(resultValue)
                        case None => Right(NoResult("No results found"))
                    }
                ),
                Duration(timeoutSeconds, TimeUnit.SECONDS)
            )
        } catch  {
            case error: TimeoutException => Left(error.getMessage())
        }
    }

    def runSparkQuery(
        queryType: Class[_ <: BaseSparkQuery],
        queryOptions: BaseQueryOptions,
        timeoutSeconds: Double
    ): Unit = {
        try {
            Await.result(
                Future(runSparkQuery(queryType, queryOptions)),
                Duration(timeoutSeconds, TimeUnit.SECONDS)
            )
        } catch  {
            case error: TimeoutException => log.error(error.getMessage())
        }
    }

    def runSparkResultQuery(
        queryCode: Int,
        queryType: Class[_ <: BaseSparkQuery],
        queryOptions: BaseQueryOptions,
        timeoutSeconds: Double
    ): Either[String, BaseResultValue] = {
        try {
            Await.result(
                Future(runSparkResultQueryUsingCache(queryCode, queryType, queryOptions)),
                Duration(timeoutSeconds, TimeUnit.SECONDS)
            )
        } catch  {
            case error: TimeoutException => Left(error.getMessage())
        }
    }

    private def logCacheUsage(code: Int, options: BaseQueryOptions): Unit = {
        log.info(
            "Using result from cache " +
            s"(${cache.getResultIndex(code, options).getOrElse(CommonConstants.MinusOne)}) " +
            s"for query ${code} with ${options}"
        )
    }

    def runSparkResultQueryUsingCache(
        queryCode: Int,
        queryType: Class[_ <: BaseSparkQuery],
        queryOptions: BaseQueryOptions
    ): Either[String, BaseResultValue] = {
        cache.getResult(queryCode, queryOptions) match {
            case Some(cachedResult: BaseResultValue) => {
                logCacheUsage(queryCode, queryOptions)
                Right(cachedResult)
            }
            case None => {
                val result: Either[String, Result] = runSparkResultQuery(queryType, queryOptions)

                result match {
                    case Right(resultValue: Result) =>
                        cache.addResult(queryCode, queryOptions, resultValue)
                    case _ =>
                }

                result
            }
        }
    }

    def runCacheResultQueryUsingCache(
        queryCode: Int,
        queryType: Class[_ <: BaseCacheQuery],
        queryOptions: BaseQueryOptions
    ): Option[BaseResultValue] = {
        cache.getResult(queryCode, queryOptions) match {
            case Some(cachedResult: BaseResultValue) => {
                logCacheUsage(queryCode, queryOptions)
                Some(cachedResult)
            }
            case None => {
                val query: BaseCacheQuery = queryType
                    .getDeclaredConstructor(queryOptions.getClass())
                    .newInstance(queryOptions)

                if (!query.cacheCheck()) {
                    val (sparkQueryType, sparkQueryOptions) = query.updateCache()
                    runSparkQuery(sparkQueryType, sparkQueryOptions)
                }

                val result: Option[BaseResultValue] = query.getResults()
                result match {
                    case Some(resultValue: BaseResultValue) =>
                        cache.addResult(queryCode, queryOptions, resultValue)
                    case None =>
                }

                result
            }
        }
    }

    def runSparkQuery[QueryOptions <: BaseQueryOptions](
        queryType: Class[_ <: BaseSparkQuery],
        queryOptions: QueryOptions
    ): Unit = {
        val sparkSession: SparkSession = SparkSessionUtils.getSparkSession()

        try {
            queryType
                .getDeclaredConstructor(queryOptions.getClass(), classOf[SparkSession])
                .newInstance(queryOptions, sparkSession)
                .runQuery()
        } catch {
            case error: java.lang.NoSuchMethodException => log.error(error.toString())
        }

        SparkSessionUtils.releaseSparkSession()
    }

    def runSparkResultQuery[QueryOptions <: BaseQueryOptions](
        queryType: Class[_ <: BaseSparkQuery],
        queryOptions: QueryOptions
    ): Either[String, Result] = {
        val sparkSession: SparkSession = SparkSessionUtils.getSparkSession()
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

        SparkSessionUtils.releaseSparkSession()
        result
    }
}
