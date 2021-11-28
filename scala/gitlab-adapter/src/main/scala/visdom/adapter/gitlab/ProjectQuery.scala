package visdom.adapter.gitlab

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.column
import spray.json.JsArray
import spray.json.JsObject
import visdom.adapter.gitlab.results.ProjectResult
import visdom.json.JsonUtils
import visdom.spark.Constants


object ProjectQuery {
    def getDataFrameFromCollection(sparkSession: SparkSession, collectionName: String): Dataset[ProjectResult] = {
        val readConfig: ReadConfig = ReadConfig(
            databaseName = Constants.DefaultDatabaseName,
            collectionName = collectionName,
            connectionString = sparkSession.sparkContext.getConf.getOption(Constants.MongoInputUriSetting)
        )

        MongoSpark.load[schemas.ProjectSchema](sparkSession, readConfig)
            .select(
                column(GitlabConstants.ColumnGroupName),
                column(GitlabConstants.ColumnProjectName)
            )
            .na.drop()
            .distinct()
            .as(Encoders.product[ProjectResult])
    }

    def getDataFrame(sparkSession: SparkSession): Dataset[ProjectResult] = {
        val commitProjects = getDataFrameFromCollection(sparkSession, GitlabConstants.CollectionCommits)
        val fileProjects = getDataFrameFromCollection(sparkSession, GitlabConstants.CollectionFiles)
        val pipelineProjects = getDataFrameFromCollection(sparkSession, GitlabConstants.CollectionPipelines)

        commitProjects
            .union(fileProjects)
            .union(pipelineProjects)
            .distinct()
    }

    def getResult(sparkSession: SparkSession): JsObject = {
        val results: Array[ProjectResult] = getDataFrame(sparkSession).collect()
        JsObject(
            results
                .groupBy(result => result.group_name)
                .mapValues(
                    projects => JsArray(
                        projects.map(
                            project => JsonUtils.toJsonValue(project.project_name)
                        ).toList
                    )
                )
        )
    }
}
