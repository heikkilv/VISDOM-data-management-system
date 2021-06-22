package visdom.spark

import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.sql.SparkSession


object Session {
    // Suppress the log messages
    Logger.getLogger("org").setLevel(Level.OFF)

    val sparkSession: SparkSession = {
        SparkSession
            .builder
            .master(Constants.SparkMaster)
            .appName(Constants.ApplicationName)
            .config(Constants.MongoInputUriSetting, Constants.DefaultMongoUri)
            .config(Constants.MongoOutputUriSetting, Constants.DefaultMongoUri)
            .getOrCreate()
    }
}
