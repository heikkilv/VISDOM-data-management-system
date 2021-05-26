package visdom.adapter.gitlab

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import com.mongodb.spark.config.ReadConfig
import com.mongodb.spark.MongoSpark
import com.mongodb.spark.rdd.MongoRDD
import org.bson.Document
import visdom.spark.Session
import visdom.spark.Constants


object Adapter {
    def main(args: Array[String]) {
        val sparkSession: SparkSession = Session.getSession()
        val sparkContext: SparkContext = sparkSession.sparkContext
        val connectionString = sparkContext.getConf.getOption(Constants.MongoInputUriSetting)

        val defaultDatabase: String = Constants.DefaultDatabaseName
        val defaultCollectionName: String = Constants.DefaultMongoCollection
        val fileCollectionName: String = "files"

        val fileReadConfig: ReadConfig = ReadConfig(
            databaseName = defaultDatabase,
            collectionName = fileCollectionName,
            connectionString = connectionString
        )

        val defaultCollection: MongoRDD[Document] = MongoSpark.load(sparkContext)
        val defaultCount: Long = defaultCollection.count()
        val fileCollection: MongoRDD[Document] = MongoSpark.load(sparkContext, fileReadConfig)
        val fileCount: Long = fileCollection.count()

        println(s"Found ${defaultCount} documents in ${defaultDatabase}.${defaultCollectionName}")
        println(s"Found ${fileCount} documents in ${defaultDatabase}.${fileCollectionName}")

        sparkSession.stop()
    }
}
