package visdom.adapter.gitlab

import org.apache.spark.sql.SparkSession
import org.bson.BsonDocument
import visdom.spark.Session
import org.bson.json.JsonWriterSettings


object Adapter {
    def main(args: Array[String]) {
        val sparkSession: SparkSession = Session.sparkSession

        val queryResult: BsonDocument = CommitQuery.getResult(sparkSession)
        println(
            queryResult.toJson(
                JsonWriterSettings.builder().indent(true).build()
            )
        )

        Session.sparkSession.stop()
    }
}
