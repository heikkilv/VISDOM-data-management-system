package visdom.adapter.gitlab

import org.apache.spark.sql.SparkSession
import org.bson.BsonDocument
import org.bson.json.JsonWriterSettings
import spray.json.JsObject
import visdom.spark.Session


object Adapter {
    def main(args: Array[String]) {
        val sparkSession: SparkSession = Session.sparkSession

        val queryResult: BsonDocument = CommitQuery.getResult(sparkSession)
        println(
            queryResult.toJson(
                JsonWriterSettings.builder().indent(true).build()
            )
        )

        val FilePaths: Array[String] = Array("README.md", ".gitignore", "adapters")
        val timestampResults: JsObject = TimestampQuery.getResult(sparkSession, FilePaths)
        println(timestampResults.prettyPrint)

        Session.sparkSession.stop()
    }
}
