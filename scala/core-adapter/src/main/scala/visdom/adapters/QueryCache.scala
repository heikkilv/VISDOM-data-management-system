package visdom.adapters

import java.time.Instant
import scala.collection.mutable
import spray.json.JsObject
import visdom.adapters.options.BaseQueryOptions
import visdom.adapters.results.BaseResultValue
import visdom.database.mongodb.MongoConnection
import visdom.utils.TimeUtils
import visdom.utils.WartRemoverConstants


class QueryCache(databases: Seq[String]) {
    private val results: mutable.Map[(Int, BaseQueryOptions), (QueryResult, Int)] = mutable.Map.empty

    @SuppressWarnings(Array(WartRemoverConstants.WartsVar))
    private var startIndex: Int = 0
    @SuppressWarnings(Array(WartRemoverConstants.WartsVar))
    private var endIndex: Int = 0

    def getResult(queryCode: Int, options: BaseQueryOptions): Option[BaseResultValue] = {
        results.get((queryCode, options)) match {
            case Some((result: QueryResult, _)) => QueryCache.getLastDatabaseUpdateTime(databases) match {
                case Some(databaseUpdateTime: Instant) => result.timestamp.compareTo(databaseUpdateTime) >= 0 match {
                    case true => Some(result.data)
                    case false => None
                }
                case None => None
            }
            case None => None
        }
    }

    def getResultIndex(queryCode: Int, options: BaseQueryOptions): Option[Int] = {
        results.get((queryCode, options)) match {
            case Some((_, index: Int)) => Some(index)
            case None => None
        }
    }

    private def checkIndexOverflow(): Unit = {
        if (endIndex == Int.MaxValue) {
            val _ = results.transform({
                case (_, (result, index)) => (result, index - startIndex)
            })
            endIndex -= startIndex
            startIndex = 0
        }
    }

    private def removeOldest(): Unit = {
        results
            .find({case (_, (_, index)) => index == startIndex + 1})
            .map({case (cacheKey, _) => cacheKey}) match {
                case Some(cacheKey) => val _ = results -= cacheKey
                case None =>
        }
    }

    def addResult(queryCode: Int, options: BaseQueryOptions, data: BaseResultValue): Unit = {
        def updateResult(targetIndex: Int): Unit = {
            results.update(
                (queryCode, options),
                (QueryResult(data, Instant.now()), targetIndex)
            )
        }

        def addResultInternal(): Unit = {
            endIndex += 1
            updateResult(endIndex)
        }

        def adjustIndexes(resultIndex: Int): Unit = {
            val _ = results.transform({
                case (_, (result, index)) => (
                    result,
                    index <= resultIndex match {
                        case true => index
                        case false => index - 1
                    }
                )
            })
        }

        results.get((queryCode, options)) match {
            case Some((result: QueryResult, resultIndex: Int)) => {
                // if (resultIndex < endIndex) {
                //     adjustIndexes(resultIndex)
                // }
                updateResult(resultIndex)
            }
            case None => {
                checkIndexOverflow()
                if (endIndex - startIndex >= QueryCache.MemoryLimit) {
                    removeOldest()
                }
                addResultInternal()
            }
        }
    }

    def clearCache(): Unit = {
        results.clear()
        startIndex = 0
        endIndex = 0
    }
}

object QueryCache {
    val MemoryLimit: Int = 500

    def getLastDatabaseUpdateTime(databases: Seq[String]): Option[Instant] = {
        def getUpdateTimeInternal(nextDatabases: Seq[String], lastUpdateTime: Option[Instant]): Option[Instant] = {
            nextDatabases.headOption match {
                case Some(database: String) => {
                    getUpdateTimeInternal(
                        nextDatabases.drop(1),
                        MongoConnection.getLastUpdateTime(database) match {
                            case Some(newUpdateTime: Instant) => Some(
                                lastUpdateTime match {
                                    case Some(oldUpdateTime: Instant) =>
                                        TimeUtils.getLaterInstant(newUpdateTime, oldUpdateTime)
                                    case None => newUpdateTime
                                }
                            )
                            case None => lastUpdateTime
                        }
                    )

                }
                case None => lastUpdateTime
            }
        }

        getUpdateTimeInternal(databases, None)
    }
}
