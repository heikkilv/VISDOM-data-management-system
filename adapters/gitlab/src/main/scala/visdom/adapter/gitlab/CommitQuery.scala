package visdom.adapter.gitlab

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.column
import org.apache.spark.sql.functions.count
import org.apache.spark.sql.functions.udf
import visdom.spark.Constants
import org.bson.BsonDocument


object CommitQuery {
    val DateStringLength: Int = 10

    def utcStringToDate: String => String = {
        datetimeString => datetimeString.substring(0, DateStringLength)
    }
    val dateStringUDF: UserDefinedFunction = udf(utcStringToDate)

    def getDataFrame(sparkSession: SparkSession): DataFrame = {
        val commitReadConfig: ReadConfig = ReadConfig(
            databaseName = Constants.DefaultDatabaseName,
            collectionName = CommitConstants.CollectionCommits,
            connectionString = sparkSession.sparkContext.getConf.getOption(
                Constants.MongoInputUriSetting
            )
        )

        MongoSpark.load[schemas.CommitSchema](sparkSession, commitReadConfig)
            .select(
                column(CommitConstants.ColumnProjectName),
                column(CommitConstants.ColumnCommitterName),
                dateStringUDF(
                    column(CommitConstants.ColumnCommittedDate)
                ).as(CommitConstants.ColumnDate)
            )
            .groupBy(
                CommitConstants.ColumnProjectName,
                CommitConstants.ColumnCommitterName,
                CommitConstants.ColumnDate
            )
            .count()
            .orderBy(
                CommitConstants.ColumnProjectName,
                CommitConstants.ColumnCommitterName,
                CommitConstants.ColumnDate
            )
    }

    def getResult(sparkSession: SparkSession): BsonDocument = {
        results.CommitResult.fromArrayToBson(
            getDataFrame(sparkSession)
                .collect()
                .map(row => results.CommitResult.fromRow(row))
        )
    }
}
