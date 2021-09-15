package visdom.spark

import com.mongodb.spark.config.ReadConfig
import org.apache.spark.sql.SparkSession


object ConfigUtils {
    def getReadConfig(sparkSession: SparkSession, databaseName: String, collectionName: String): ReadConfig = {
        ReadConfig(
            databaseName = databaseName,
            collectionName = collectionName,
            connectionString = sparkSession.sparkContext.getConf.getOption(Constants.MongoInputUriSetting)
        )
    }
}
