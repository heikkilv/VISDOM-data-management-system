package test

import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.sql.SparkSession


object Adapter {
    def main(args: Array[String]) {
        // Suppress the log messages
        Logger.getLogger("org").setLevel(Level.OFF)

        val EnvironmentApplicationName: String = "APPLICATION_NAME"
        val EnvironmentSparkMaster: String = "SPARK_MASTER_NAME"
        val EnvironmentSparkPort: String = "SPARK_MASTER_PORT"
        val DefaultApplicationName: String = "Adapter"
        val DefaultSparkMaster: String = "spark-master"
        val DefaultSparkPort: String = "7707"

        val applicationName: String = sys.env.getOrElse(EnvironmentApplicationName, DefaultApplicationName)
        val sparkMaster: String = "spark://" + Seq(
            sys.env.getOrElse(EnvironmentSparkMaster, DefaultSparkMaster),
            sys.env.getOrElse(EnvironmentSparkPort, DefaultSparkPort)
        ).mkString(":")

        val spark: SparkSession = SparkSession
            .builder
            .master(sparkMaster)
            .appName(applicationName)
            .getOrCreate()

        spark.stop()
    }
}
