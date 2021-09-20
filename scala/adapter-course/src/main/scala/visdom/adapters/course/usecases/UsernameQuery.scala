package visdom.adapters.course.usecases

import com.mongodb.spark.MongoSpark
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.column
import spray.json.JsObject
import visdom.adapters.course.AdapterValues
import visdom.adapters.course.AdapterValues.aPlusDatabaseName
import visdom.adapters.course.options.UsernameQueryOptions
import visdom.adapters.course.output.UsernameOutput
import visdom.adapters.course.schemas.SimplePointSchema
import visdom.database.mongodb.MongoConstants
import visdom.spark.ConfigUtils
import visdom.spark.Session
import visdom.utils.SnakeCaseConstants


class UsernameQuery(queryOptions: UsernameQueryOptions) {
    val queryCode: Int = 2
    val sparkSession: SparkSession = Session.getSparkSession()

    def getUsernames(): Seq[String] = {
        MongoSpark
            .load[SimplePointSchema](
                sparkSession,
                ConfigUtils.getReadConfig(sparkSession, aPlusDatabaseName, MongoConstants.CollectionPoints)
            )
            .cache()
            .filter(column(SnakeCaseConstants.CourseId) === queryOptions.courseId)
            .collect()
            .flatMap(row => SimplePointSchema.fromRow(row))
            .map(simplePoint => simplePoint.username)
    }

    def getResults(): JsObject = {
        AdapterValues.cache.getResult(queryCode, queryOptions) match {
            case Some(cachedResult: JsObject) => {
                println(s"Using result from cache for query ${queryCode} with ${queryOptions}")
                cachedResult
            }
            case None => {
                val usernames: Seq[String] = getUsernames()
                val result: JsObject = UsernameOutput(usernames).toJsObject()

                AdapterValues.cache.addResult(queryCode, queryOptions, result)
                result
            }
        }
    }
}
