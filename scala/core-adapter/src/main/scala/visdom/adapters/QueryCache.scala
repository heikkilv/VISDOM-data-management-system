package visdom.adapters

import java.time.Instant
import scala.collection.mutable
import spray.json.JsObject
import visdom.adapters.options.BaseQueryOptions
import visdom.adapters.results.BaseResultValue
import visdom.database.mongodb.MongoConnection
import visdom.utils.TimeUtils


class QueryCache(databases: Seq[String]) {
    private val results: mutable.Map[(Int, BaseQueryOptions), QueryResult] = mutable.Map.empty

    def getResult(queryCode: Int, options: BaseQueryOptions): Option[BaseResultValue] = {
        results.get((queryCode, options)) match {
            case Some(result: QueryResult) => QueryCache.getLastDatabaseUpdateTime(databases) match {
                case Some(databaseUpdateTime: Instant) => result.timestamp.compareTo(databaseUpdateTime) >= 0 match {
                    case true => Some(result.data)
                    case false => None
                }
                case None => None
            }
            case None => None
        }
    }

    def addResult(queryCode: Int, options: BaseQueryOptions, data: BaseResultValue): Unit = {
        val _ = results += (((queryCode, options), QueryResult(data, Instant.now())))
    }
}

object QueryCache {
    def getLastDatabaseUpdateTime(databases: Seq[String]): Option[Instant] = {
        def getUpdateTimeInternal(nextDatabases: Seq[String], lastUpdateTime: Option[Instant]): Option[Instant] = {
            nextDatabases.headOption match {
                case Some(database: String) => {
                    MongoConnection.getLastUpdateTime(database) match {
                        case Some(newUpdateTime: Instant) =>
                            getUpdateTimeInternal(
                                nextDatabases.drop(1),
                                Some(
                                    lastUpdateTime match {
                                        case Some(oldUpdateTime: Instant) =>
                                            TimeUtils.getLaterInstant(newUpdateTime, oldUpdateTime)
                                        case None => newUpdateTime
                                    }
                                )
                            )
                        case None => None
                    }
                }
                case None => lastUpdateTime
            }
        }

        getUpdateTimeInternal(databases, None)
    }
}
