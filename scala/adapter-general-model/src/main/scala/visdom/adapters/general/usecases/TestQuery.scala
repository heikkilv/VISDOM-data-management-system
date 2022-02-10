package visdom.adapters.general.usecases

import com.mongodb.spark.MongoSpark
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.results.ArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.FileArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.GitlabAuthorResult
import visdom.adapters.general.model.results.EventResult
import visdom.adapters.general.model.results.EventResult.CommitEventResult
import visdom.adapters.general.model.results.OriginResult
import visdom.adapters.general.model.results.OriginResult.GitlabOriginResult
import visdom.adapters.general.schemas.CommitAuthorSimpleSchema
import visdom.adapters.general.schemas.FileSchema
import visdom.adapters.general.schemas.GitlabAuthorSchema
import visdom.adapters.options.TestQueryOptions
import visdom.adapters.options.TestTargetArtifact
import visdom.adapters.options.TestTargetAuthor
import visdom.adapters.options.TestTargetEvent
import visdom.adapters.options.TestTargetOrigin
import visdom.adapters.queries.BaseQuery
import visdom.adapters.queries.IncludesQueryCode
import visdom.adapters.general.AdapterValues
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.general.schemas.GitlabProjectSchema
import visdom.adapters.results.BaseResultValue
import visdom.adapters.results.Result
import visdom.adapters.utils.AdapterUtils
import visdom.database.mongodb.MongoConstants
import visdom.spark.ConfigUtils
import visdom.utils.QueryUtils.EnrichedDataSet
import visdom.utils.SnakeCaseConstants


class TestQuery(queryOptions: TestQueryOptions, sparkSession: SparkSession)
extends BaseQuery(queryOptions, sparkSession) {
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newStringEncoder

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

    def getAuthors(): Dataset[GitlabAuthorResult] = {
        MongoSpark
            .load[CommitAuthorSimpleSchema](
                sparkSession,
                ConfigUtils.getReadConfig(
                    sparkSession,
                    AdapterValues.gitlabDatabaseName,
                    MongoConstants.CollectionCommits
                )
            )
            .flatMap(row => CommitAuthorSimpleSchema.fromRow(row))
            .map(commitSchema => GitlabAuthorSchema.fromCommitAuthorSimpleSchema(commitSchema))
            .groupByKey(authorSchema => (authorSchema.host_name, authorSchema.committer_email))
            .reduceGroups(
                (firstAuthorSchema, secondAuthorSchema) =>
                    GitlabAuthorSchema.reduceSchemas(firstAuthorSchema, secondAuthorSchema)
            )
            .map({case (_, authorSchema) => ArtifactResult.fromGitlabAuthorSchema(authorSchema)})
    }

    def getArtifacts(): Dataset[FileArtifactResult] = {
        MongoSpark
            .load[FileSchema](
                sparkSession,
                ConfigUtils.getReadConfig(
                    sparkSession,
                    AdapterValues.gitlabDatabaseName,
                    MongoConstants.CollectionFiles
                )
            )
            .flatMap(row => FileSchema.fromRow(row))
            .map(fileSchema => ArtifactResult.fromFileSchema(fileSchema))
    }

    def getResults(): Result = {
        val results: Dataset[_ <: BaseResultValue] = queryOptions.target match {
            case TestTargetEvent => getEvents()
            case TestTargetOrigin => getOrigins()
            case TestTargetAuthor => getAuthors()
            case TestTargetArtifact => getArtifacts()
        }
        AdapterUtils.getResult(results, queryOptions, SnakeCaseConstants.Id)
    }
}

object TestQuery extends IncludesQueryCode {
    val queryCode: Int = 101
}
