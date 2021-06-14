package visdom.adapter.gitlab

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.column
import spray.json.JsObject
import visdom.adapter.gitlab.queries.timestamps.TimestampQueryOptions
import visdom.adapter.gitlab.results.TimestampResult
import visdom.adapter.gitlab.utils.JsonUtils
import visdom.spark.Constants


object TimestampQuery {
    def getCommitTimestampMap(
        sparkSession: SparkSession,
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
                column(GitlabConstants.ColumnCreatedAt)
            )
            .as(Encoders.product[schemas.CommitTimestampSchema])
            .cache()

        // collected map with project names and commit ids as keys and the commit timestamps as values
        commitDataFrame
            .filter(
                row => projectCommits.contains(schemas.FileDistinctCommitSchema(row.project_name, row.id))
            )
            .collect()
            .map(x => (schemas.CommitTimestampSchemaKey(x.project_name, x.id), x.created_at))
            .toMap
    }

    def getDataFrame(
        sparkSession: SparkSession,
        queryOptions: TimestampQueryOptions
    ): Dataset[TimestampResult] = {
        import sparkSession.implicits.newProductEncoder

        // read configuration for the files collection
        val fileReadConfig: ReadConfig = ReadConfig(
            databaseName = Constants.DefaultDatabaseName,
            collectionName = GitlabConstants.CollectionFiles,
            connectionString = sparkSession.sparkContext.getConf.getOption(
                Constants.MongoInputUriSetting
            )
        )

        // dataset for files with the required paths
        val fileDataFrame: Dataset[schemas.FileCommitSchema] = MongoSpark
            .load[schemas.FileSchema](sparkSession, fileReadConfig)
            .select(
                column(GitlabConstants.ColumnProjectName),
                column(GitlabConstants.ColumnPath),
                column(GitlabConstants.ColumnLinksCommits).as(GitlabConstants.ColumnCommits)
            )
            .as(Encoders.product[schemas.FileCommitSchema])
            .filter(row => queryOptions.filePaths.contains(row.path))
            .cache()

        // collection for all the commits (for the relevant files) in each projects
        val projectCommits: Array[schemas.FileDistinctCommitSchema] = fileDataFrame
            .flatMap(row => row.commits.map(x => schemas.FileDistinctCommitSchema(row.project_name, x)))
            .distinct()
            .collect()

        val timestampDataMap: Map[schemas.CommitTimestampSchemaKey, String] =
            getCommitTimestampMap(sparkSession, projectCommits)

        // dataset where the commit ids are mapped to the commit timestamps
        val fileDataFrameWithTimestamps: Dataset[TimestampResult] = fileDataFrame
            .map(
                row => TimestampResult(
                    row.project_name,
                    row.path,
                    row.commits.map(
                        commitId => timestampDataMap(
                            schemas.CommitTimestampSchemaKey(row.project_name, commitId)
                        )
                    ).sorted
                )
            )

        fileDataFrameWithTimestamps
    }

    def getResult(sparkSession: SparkSession, queryOptions: TimestampQueryOptions): JsObject = {
        val results: Array[TimestampResult] = getDataFrame(sparkSession, queryOptions).collect()
        JsonUtils.toJsObject(results.map(result => result.toJsonTuple()))
    }
}
