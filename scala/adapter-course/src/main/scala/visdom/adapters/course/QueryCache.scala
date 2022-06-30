// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.course

import java.time.Instant
import scala.collection.mutable
import spray.json.JsObject
import visdom.adapters.course.options.BaseQueryOptions
import visdom.database.mongodb.MongoConnection
import visdom.utils.TimeUtils


final case class QueryResult(
    data: JsObject,
    timestamp: Instant
)

class QueryCache(databases: Seq[String]) {
    private val results: mutable.Map[(Int, BaseQueryOptions), QueryResult] = mutable.Map.empty

    def getResult(queryCode: Int, options: BaseQueryOptions): Option[JsObject] = {
        results.get((queryCode, options)) match {
            case Some(result: QueryResult) => getLastDatabaseUpdateTime() match {
                case Some(databaseUpdateTime: Instant) => result.timestamp.compareTo(databaseUpdateTime) >= 0 match {
                    case true => Some(result.data)
                    case false => None
                }
                case None => None
            }
            case None => None
        }
    }

    def addResult(queryCode: Int, options: BaseQueryOptions, data: JsObject): Unit = {
        val _ = results += (((queryCode, options), QueryResult(data, Instant.now())))
    }

    private def getLastDatabaseUpdateTime(): Option[Instant] = {
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
