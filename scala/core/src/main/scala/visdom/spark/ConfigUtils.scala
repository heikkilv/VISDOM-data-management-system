package visdom.spark

import com.mongodb.spark.config.ReadConfig
import com.mongodb.spark.config.WriteConfig
import org.apache.spark.sql.SparkSession


object ConfigUtils {
    def getReadConfig(sparkSession: SparkSession, databaseName: String, collectionName: String): ReadConfig = {
        ReadConfig(
            databaseName = databaseName,
            collectionName = collectionName,
            connectionString = sparkSession.sparkContext.getConf.getOption(Constants.MongoInputUriSetting)
        )
    }

    def getWriteConfig(sparkSession: SparkSession, databaseName: String, collectionName: String): WriteConfig = {
        WriteConfig(
            databaseName = databaseName,
            collectionName = collectionName,
            connectionString = sparkSession.sparkContext.getConf.getOption(Constants.MongoOutputUriSetting),
            replaceDocument = true
        )
    }
}
