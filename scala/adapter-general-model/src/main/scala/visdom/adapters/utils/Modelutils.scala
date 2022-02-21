package visdom.adapters.utils

import com.mongodb.spark.MongoSpark
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.Document
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.general.AdapterValues
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.Origin
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.model.results.ArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.FileArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.GitlabAuthorResult
import visdom.adapters.general.model.results.EventResult
import visdom.adapters.general.model.results.EventResult.CommitEventResult
import visdom.adapters.general.model.results.EventResult.PipelineEventResult
import visdom.adapters.general.model.results.OriginResult
import visdom.adapters.general.model.results.OriginResult.GitlabOriginResult
import visdom.adapters.general.schemas.CommitAuthorSimpleSchema
import visdom.adapters.general.schemas.FileSchema
import visdom.adapters.general.schemas.GitlabAuthorSchema
import visdom.adapters.general.schemas.GitlabProjectSchema
import visdom.adapters.general.schemas.PipelineSchema
import visdom.database.mongodb.MongoConnection
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.spark.ConfigUtils
import visdom.utils.SnakeCaseConstants


class ModelUtils(sparkSession: SparkSession) {
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newStringEncoder

    def getCommits(): Dataset[CommitEventResult] = {
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

    def getPipelines(): Dataset[PipelineEventResult] = {
        MongoSpark
            .load[PipelineSchema](
                sparkSession,
                ConfigUtils.getReadConfig(
                    sparkSession,
                    AdapterValues.gitlabDatabaseName,
                    MongoConstants.CollectionPipelines
                )
            )
            .flatMap(row => PipelineSchema.fromRow(row))
            .map(pipelineSchema => EventResult.fromPipelineSchema(pipelineSchema))
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

    def getGitlabOrigins(): Dataset[GitlabOriginResult] = {
        getAllGitlabProjects()
            .map(projectSchema => OriginResult.fromGitlabProjectSchema(projectSchema))
    }

    def getGitlabAuthors(): Dataset[GitlabAuthorResult] = {
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

    def getFiles(): Dataset[FileArtifactResult] = {
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

    def updateOrigins(): Unit = {
        if (!ModuleUtils.isOriginCacheUpdated()) {
            GeneralQueryUtils.storeObjects(sparkSession, getGitlabOrigins(), GitlabOrigin.GitlabOriginType)
            updateOriginsIndexes()
        }
    }

    def updateEvents(): Unit = {
        if (!ModuleUtils.isEventCacheUpdated()) {
            GeneralQueryUtils.storeObjects(sparkSession, getCommits(), CommitEvent.CommitEventType)
            GeneralQueryUtils.storeObjects(sparkSession, getPipelines(), PipelineEvent.PipelineEventType)
            updateEventIndexes()
        }
    }

    def updateAuthors(updateIndex: Boolean): Unit = {
        if (!ModuleUtils.isAuthorCacheUpdated()) {
            GeneralQueryUtils.storeObjects(sparkSession, getGitlabAuthors(), GitlabAuthor.GitlabAuthorType)
            if (updateIndex) {
                updateArtifactIndexes()
            }
        }
    }

    def updateArtifacts(): Unit = {
        updateAuthors(false)
        if (!ModuleUtils.isArtifactCacheUpdated()) {
            GeneralQueryUtils.storeObjects(sparkSession, getFiles(), FileArtifact.FileArtifactType)
            updateArtifactIndexes()
        }
    }

    private def getCacheDocuments(
        objectTypes: Seq[String]
    ): Seq[(String, MongoCollection[Document], List[BsonDocument])] = {
        objectTypes
                .map(
                    objectType => (
                        objectType,
                        MongoConnection.getCollection(AdapterValues.cacheDatabaseName, objectType)
                    )
                )
                .map({
                    case (objectType, collection) => (
                        objectType,
                        collection,
                        MongoConnection
                            .getDocuments(collection, List.empty)
                            .map(document => document.toBsonDocument)
                    )
                })
    }

    def updateIndexes(objectTypes: Seq[String]): Unit = {
        val cacheDocuments: Seq[(String, MongoCollection[Document], List[BsonDocument])] =
            getCacheDocuments(objectTypes)

        val indexMap: Map[String, Indexes] =
            cacheDocuments
                .map({
                    case (objectType, _, documentList) =>
                        documentList
                            .flatMap(document => document.getStringOption(SnakeCaseConstants.Id))
                            .sorted
                            .zipWithIndex
                            .map({case (id, index) => (id, index, objectType)})
                })
                .flatten
                .sortBy({case (id, _, _) => id})
                .zipWithIndex
                .map({case ((id, typeIndex, typeString), categoryIndex) => (id, Indexes(categoryIndex + 1, typeIndex + 1))})
                .toMap

        val documentUpdates: Seq[Unit] = cacheDocuments.map({
            case (_, collection, documentList) =>
                documentList.map(
                    document => document.getStringOption(SnakeCaseConstants.Id) match {
                        case Some(id: String) => indexMap.get(id) match {
                            case Some(indexes: Indexes) =>
                                document
                                    .append(SnakeCaseConstants.CategoryIndex, JsonUtils.toBsonValue(indexes.categoryIndex))
                                    .append(SnakeCaseConstants.TypeIndex, JsonUtils.toBsonValue(indexes.typeIndex))
                            case None => document
                        }
                        case None => document
                    }
                )
                .foreach(
                    document => MongoConnection.storeDocument(collection, document, Array(SnakeCaseConstants.Id))
                )
        })
    }

    def updateOriginsIndexes(): Unit = {
        updateIndexes(Seq(GitlabOrigin.GitlabOriginType))
    }

    def updateEventIndexes(): Unit = {
        updateIndexes(Seq(CommitEvent.CommitEventType))
    }

    def updateArtifactIndexes(): Unit = {
        updateIndexes(Seq(FileArtifact.FileArtifactType, GitlabAuthor.GitlabAuthorType))
    }

    def updateTargetCache(targetType: String): Unit = {
        targetType match {
            case Event.EventType => updateEvents()
            case Origin.OriginType => updateOrigins()
            case Artifact.ArtifactType => updateArtifacts()
            case Author.AuthorType => updateArtifacts()
            case _ =>
        }
    }
}

object ModuleUtils {
    def isOriginCacheUpdated(): Boolean = {
        GeneralQueryUtils.isCacheUpdated(GitlabOrigin.GitlabOriginType)
    }

    def isEventCacheUpdated(): Boolean = {
        GeneralQueryUtils.isCacheUpdated(CommitEvent.CommitEventType) &&
        GeneralQueryUtils.isCacheUpdated(PipelineEvent.PipelineEventType)
    }

    def isAuthorCacheUpdated(): Boolean = {
        GeneralQueryUtils.isCacheUpdated(GitlabAuthor.GitlabAuthorType)
    }

    def isArtifactCacheUpdated(): Boolean = {
        isAuthorCacheUpdated() &&
        GeneralQueryUtils.isCacheUpdated(FileArtifact.FileArtifactType)
    }

    def isTargetCacheUpdated(targetType: String): Boolean = {
        targetType match {
            case Event.EventType => isEventCacheUpdated()
            case Origin.OriginType => isOriginCacheUpdated()
            case Artifact.ArtifactType => isArtifactCacheUpdated()
            case Author.AuthorType => isAuthorCacheUpdated()
            case _ => false
        }
    }
}
