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
import visdom.http.server.adapter.gitlab.TimestampQueryOptionsChecked
import visdom.adapter.gitlab.results.TimestampResult
import visdom.adapter.gitlab.utils.JsonUtils
import visdom.spark.Constants
import java.time.ZonedDateTime
import visdom.adapter.gitlab.utils.TimeUtils


object TimestampQuery {
    implicit class EnrichedDataSet[T](dataset: Dataset[T]) {
        def applyProjectFilter(projectNameOption: Option[String]): Dataset[T] = {
            projectNameOption match {
                case Some(projectName: String) =>
                    dataset.filter(column(GitlabConstants.ColumnProjectName) === projectName)
                case None => dataset
            }
        }

        def applyDateTimeFilter(dateStringOption: Option[String], isStartFilter: Boolean): Dataset[T] = {
            dateStringOption match {
                case Some(dateString: String) => isStartFilter match {
                    case true => dataset.filter(column(GitlabConstants.ColumnCommittedDate) >= dateString)
                    case false => dataset.filter(column(GitlabConstants.ColumnCommittedDate) <= dateString)
                }
                case None => dataset
            }
        }

        def applyFilters(queryOptions: TimestampQueryOptionsChecked): Dataset[T] = {
            dataset
                .applyProjectFilter(queryOptions.projectName)
                .applyDateTimeFilter(queryOptions.startDate, true)
                .applyDateTimeFilter(queryOptions.endDate, false)
        }
    }

    def utcStringToDateTime: String => Option[String] = {
        datetimeString => TimeUtils.toUtcString(datetimeString)
    }
    val dateTimeStringUDF: UserDefinedFunction = udf(utcStringToDateTime)

    // read configuration for the files collection
    def getFileReadConfig(sparkSession: SparkSession): ReadConfig = {
        ReadConfig(
            databaseName = Constants.DefaultDatabaseName,
            collectionName = GitlabConstants.CollectionFiles,
            connectionString = sparkSession.sparkContext.getConf.getOption(
                Constants.MongoInputUriSetting
            )
        )
    }

    def getCommitTimestampMap(
        sparkSession: SparkSession,
        queryOptions: TimestampQueryOptionsChecked,
        projectCommits: Array[schemas.FileDistinctCommitSchema]
    ): Map[schemas.CommitTimestampSchemaKey, String] = {
        // read configuration for the commits collection
        val commitReadConfig: ReadConfig = ReadConfig(
            databaseName = Constants.DefaultDatabaseName,
            collectionName = GitlabConstants.CollectionCommits,
            connectionString = sparkSession.sparkContext.getConf.getOption(
                Constants.MongoInputUriSetting
            )
        )

        // dataset for all available commits
        val commitDataFrame: Dataset[schemas.CommitTimestampSchema] = MongoSpark
            .load[schemas.CommitTimestampSchema](sparkSession, commitReadConfig)
            .select(
                column(GitlabConstants.ColumnProjectName),
                column(GitlabConstants.ColumnId),
                dateTimeStringUDF(
                    column(GitlabConstants.ColumnCommittedDate)
                ).as(GitlabConstants.ColumnCommittedDate)
            )
            .applyFilters(queryOptions)
            .as(Encoders.product[schemas.CommitTimestampSchema])
            .cache()

        // collected map with project names and commit ids as keys and the commit timestamps as values
        commitDataFrame
            .filter(
                row => projectCommits.contains(schemas.FileDistinctCommitSchema(row.project_name, row.id))
            )
            .collect()
            .map(x => (schemas.CommitTimestampSchemaKey(x.project_name, x.id), x.committed_date))
            .toMap
    }

    def getDataFrame(
        sparkSession: SparkSession,
        queryOptions: TimestampQueryOptionsChecked
    ): Dataset[TimestampResult] = {
        import sparkSession.implicits.newProductEncoder

        // read configuration for the files collection
        val fileReadConfig: ReadConfig = getFileReadConfig(sparkSession)

        // dataset for files with the required paths
        val fileDataFrame: Dataset[schemas.FileCommitSchema] = MongoSpark
            .load[schemas.FileSchema](sparkSession, fileReadConfig)
            .select(
                column(GitlabConstants.ColumnProjectName),
                column(GitlabConstants.ColumnPath),
                column(GitlabConstants.ColumnLinksCommits).as(GitlabConstants.ColumnCommits)
            )
            .applyProjectFilter(queryOptions.projectName)
            .as(Encoders.product[schemas.FileCommitSchema])
            .filter(row => queryOptions.filePaths.contains(row.path))
            .cache()

        // collection for all the commits (for the relevant files) in each projects
        val projectCommits: Array[schemas.FileDistinctCommitSchema] = fileDataFrame
            .flatMap(row => row.commits.map(x => schemas.FileDistinctCommitSchema(row.project_name, x)))
            .distinct()
            .collect()

        val timestampDataMap: Map[schemas.CommitTimestampSchemaKey, String] =
            getCommitTimestampMap(sparkSession, queryOptions, projectCommits)

        // dataset where the commit ids are mapped to the commit timestamps
        val fileDataFrameWithTimestamps: Dataset[TimestampResult] = fileDataFrame
            .map(
                row => TimestampResult(
                    row.project_name,
                    row.path,
                    row.commits.map(
                        commitId => timestampDataMap.get(
                            schemas.CommitTimestampSchemaKey(row.project_name, commitId)
                        )
                    ).flatten.sorted
                )
            )

        fileDataFrameWithTimestamps
    }

    def getResult(sparkSession: SparkSession, queryOptions: TimestampQueryOptionsChecked): JsObject = {
        val results: Array[TimestampResult] = getDataFrame(sparkSession, queryOptions).collect()
        JsonUtils.toJsObject(results.map(result => result.toJsonTuple()))
    }
}
