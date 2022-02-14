package visdom.adapters.general.usecases

import com.mongodb.spark.MongoSpark
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import spray.json.JsObject
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
import visdom.adapters.options.ObjectTypes
import visdom.adapters.options.SingleQueryOptions
import visdom.adapters.queries.BaseQuery
import visdom.adapters.queries.IncludesQueryCode
import visdom.adapters.general.AdapterValues
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.general.schemas.GitlabProjectSchema
import visdom.adapters.results.BaseResultValue
import visdom.adapters.results.IdValue
import visdom.adapters.results.MultiResult
import visdom.adapters.results.Result
import visdom.adapters.results.ResultCounts
import visdom.adapters.results.SingleResult
import visdom.adapters.utils.AdapterUtils
import visdom.database.mongodb.MongoConstants
import visdom.spark.ConfigUtils
import visdom.utils.QueryUtils.EnrichedDataSet
import visdom.utils.SnakeCaseConstants


class SingleQuery(queryOptions: SingleQueryOptions, sparkSession: SparkSession)
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

    def getSingleResult(): Option[Dataset[_ <: BaseResultValue]] = {
        (
            queryOptions.objectType match {
                case eventType: String if ObjectTypes.EventTypes.contains(eventType) => Some(getEvents())
                case originType: String if ObjectTypes.OriginTypes.contains(originType) => Some(getOrigins())
                case authorType: String if ObjectTypes.AuthorTypes.contains(authorType) => Some(getAuthors())
                case artifactType: String if ObjectTypes.ArtifactTypes.contains(artifactType) => Some(getArtifacts())
                case _ => None
            }
        )
            .map(dataset => SingleQuery.getItem(dataset, queryOptions.uuid))
    }

    def getResults(): Result = {
        getSingleResult() match {
            case Some(fetchedResults) => fetchedResults.count().toInt match {
                case n if n >= 1 => {
                    Result(
                        counts = ResultCounts.getSingleResultCounts(),
                        results = fetchedResults.head().toJsValue() match {
                            case resultJsObject: JsObject => SingleResult(resultJsObject)
                            case _ => MultiResult(Seq.empty)
                        }
                    )
                }
                case _ => Result.getEmpty()
            }
            case None => Result.getEmpty()
        }
    }
}

object SingleQuery extends IncludesQueryCode with Serializable {
    val queryCode: Int = 102

    def getItem[T <: IdValue](dataset: Dataset[T], uuid: String): Dataset[T] = {
        def filterFunction[T <: IdValue](item: T): Boolean = {
            item.id == uuid
        }

        dataset.filter(filterFunction _)
    }
}
