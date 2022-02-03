package visdom.adapters.general.usecases

import com.mongodb.spark.MongoSpark
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.general.model.results.EventResult
import visdom.adapters.general.model.results.EventResult.CommitEventResult
import visdom.adapters.general.model.results.OriginResult
import visdom.adapters.general.model.results.OriginResult.GitlabOriginResult
import visdom.adapters.options.TestQueryOptions
import visdom.adapters.options.TestTargetEvent
import visdom.adapters.options.TestTargetOrigin
import visdom.adapters.queries.BaseQuery
import visdom.adapters.queries.IncludesQueryCode
import visdom.adapters.general.AdapterValues
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.general.schemas.GitlabProjectSchema
import visdom.adapters.results.Result
import visdom.adapters.utils.AdapterUtils
import visdom.database.mongodb.MongoConstants
import visdom.spark.ConfigUtils
import visdom.utils.QueryUtils.EnrichedDataSet
import visdom.utils.SnakeCaseConstants
import visdom.adapters.results.BaseResultValue


class TestQuery(queryOptions: TestQueryOptions, sparkSession: SparkSession)
extends BaseQuery(queryOptions, sparkSession) {
    import sparkSession.implicits.newProductEncoder

    def getEvents(): Dataset[CommitEventResult] = {
        MongoSpark
            .load[CommitSchema](
                sparkSession,
                ConfigUtils.getReadConfig(
                    sparkSession,
                    AdapterValues.gitlabDatabaseName,
                    MongoConstants.CollectionCommits
                )
            )
            .applyContainsFilter(SnakeCaseConstants.Message, queryOptions.token)
            .flatMap(row => CommitSchema.fromRow(row))
            .map(commitSchema => EventResult.fromCommitSchema(commitSchema))
    }

    def getGitlabProjects(collectionName: String): Dataset[GitlabProjectSchema] = {
        MongoSpark
            .load[GitlabProjectSchema](
                sparkSession,
                ConfigUtils.getReadConfig(
                    sparkSession,
                    AdapterValues.gitlabDatabaseName,
                    collectionName
                )
            )
            .na.drop()
            .distinct()
            .flatMap(row => GitlabProjectSchema.fromRow(row))
    }

    def getAllGitlabProjects(): Dataset[GitlabProjectSchema] = {
        val commitProjects = getGitlabProjects(MongoConstants.CollectionCommits)
        val fileProjects = getGitlabProjects(MongoConstants.CollectionFiles)
        val pipelineProjects = getGitlabProjects(MongoConstants.CollectionPipelines)

        commitProjects
            .union(fileProjects)
            .union(pipelineProjects)
            .distinct()

        // NOTE: the project documents from projects collection could also be used for this
    }

    def getOrigins(): Dataset[GitlabOriginResult] = {
        getAllGitlabProjects()
            .map(projectSchema => OriginResult.fromGitlabProjectSchema(projectSchema))
    }

    def getResults(): Result = {
        val results: Dataset[_ <: BaseResultValue] = queryOptions.target match {
            case TestTargetEvent => getEvents()
            case TestTargetOrigin => getOrigins()
        }
        AdapterUtils.getResult(results, queryOptions, SnakeCaseConstants.Id)
    }
}

object TestQuery extends IncludesQueryCode {
    val queryCode: Int = 101
}
