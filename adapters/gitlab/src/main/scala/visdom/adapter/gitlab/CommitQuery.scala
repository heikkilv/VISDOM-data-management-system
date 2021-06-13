package visdom.adapter.gitlab

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.column
import org.apache.spark.sql.functions.udf
import spray.json.JsObject
import visdom.adapter.gitlab.results.CommitResult
import visdom.adapter.gitlab.utils.JsonUtils
import visdom.spark.Constants


object CommitQuery {
    val DateStringLength: Int = 10

    def utcStringToDate: String => String = {
        datetimeString => datetimeString.substring(0, DateStringLength)
    }
    val dateStringUDF: UserDefinedFunction = udf(utcStringToDate)

    def getDataFrame(sparkSession: SparkSession): Dataset[CommitResult] = {
        val commitReadConfig: ReadConfig = ReadConfig(
            databaseName = Constants.DefaultDatabaseName,
            collectionName = GitlabConstants.CollectionCommits,
            connectionString = sparkSession.sparkContext.getConf.getOption(
                Constants.MongoInputUriSetting
            )
        )

        MongoSpark.load[schemas.CommitSchema](sparkSession, commitReadConfig)
            .select(
                column(GitlabConstants.ColumnProjectName),
                column(GitlabConstants.ColumnCommitterName),
                dateStringUDF(
                    column(GitlabConstants.ColumnCommittedDate)
                ).as(GitlabConstants.ColumnDate)
            )
            .groupBy(
                GitlabConstants.ColumnProjectName,
                GitlabConstants.ColumnCommitterName,
                GitlabConstants.ColumnDate
            )
            .count()
            .as(Encoders.product[CommitResult])
    }

    def getResult(sparkSession: SparkSession): JsObject = {
        val results: Array[CommitResult] = getDataFrame(sparkSession).collect()
        JsonUtils.toJsObject(results.map(result => result.toJsonTuple()))
    }
}
