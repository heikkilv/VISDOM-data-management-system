package visdom.adapter.gitlab

import org.apache.spark.sql.SparkSession
import spray.json.JsObject
import visdom.spark.Session


object Adapter {
    def main(args: Array[String]) {
        val sparkSession: SparkSession = Session.sparkSession

        val commitResult: JsObject = CommitQuery.getResult(sparkSession)
        println(commitResult.prettyPrint)

        val FilePaths: Array[String] = Array("README.md", ".gitignore", "adapters")
        val timestampResults: JsObject = TimestampQuery.getResult(sparkSession, FilePaths)
        println(timestampResults.prettyPrint)

        Session.sparkSession.stop()
    }
}
