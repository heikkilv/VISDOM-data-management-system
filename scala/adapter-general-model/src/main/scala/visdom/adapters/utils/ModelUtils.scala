package visdom.adapters.utils

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.Document
import scala.reflect.runtime.universe.TypeTag
import visdom.adapters.general.AdapterValues
import visdom.adapters.general.model.authors.CommitAuthor
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.artifacts.PipelineReportArtifact
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.Origin
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.options.ObjectTypes
import visdom.adapters.general.schemas.PipelineJobSchema
import visdom.adapters.general.schemas.PipelineSchema
import visdom.database.mongodb.MongoConnection
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.spark.ConfigUtils
import visdom.utils.SnakeCaseConstants


class ModelUtils(sparkSession: SparkSession) {
    import sparkSession.implicits.newProductEncoder

    private val originUtils: ModelOriginUtils = new ModelOriginUtils(sparkSession, this)
    private val eventUtils: ModelEventUtils = new ModelEventUtils(sparkSession, this)
    private val artifactUtils: ModelArtifactUtils = new ModelArtifactUtils(sparkSession, this)
    private val authorUtils: ModelAuthorUtils = new ModelAuthorUtils(sparkSession, this)

    def getPipelineSchemas(): Dataset[PipelineSchema] = {
        loadMongoData[PipelineSchema](MongoConstants.CollectionPipelines)
            .flatMap(row => PipelineSchema.fromRow(row))
    }

    def getPipelineJobSchemas(): Dataset[PipelineJobSchema] = {
        loadMongoData[PipelineJobSchema](MongoConstants.CollectionJobs)
            .flatMap(row => PipelineJobSchema.fromRow(row))
    }

    def getPipelineProjectNames(): Map[Int, String] = {
        getPipelineSchemas()
            .map(pipelineSchema => (pipelineSchema.id, pipelineSchema.project_name))
            .collect()
            .toMap
    }

    def updateOrigins(): Unit = {
        if (!ModelUtils.isOriginCacheUpdated()) {
            storeObjects(originUtils.getGitlabOrigins(), GitlabOrigin.GitlabOriginType)
            updateOriginsIndexes()
        }
    }

    def updateEvents(): Unit = {
        if (!ModelUtils.isEventCacheUpdated()) {
            storeObjects(eventUtils.getCommits(), CommitEvent.CommitEventType)
            storeObjects(eventUtils.getPipelines(), PipelineEvent.PipelineEventType)
            storeObjects(eventUtils.getPipelineJobs(), PipelineJobEvent.PipelineJobEventType)
            updateEventIndexes()
        }
    }

    def updateAuthors(): Unit = {
        if (!ModelUtils.isAuthorCacheUpdated()) {
            storeObjects(authorUtils.getCommitAuthors(), CommitAuthor.CommitAuthorType)
            storeObjects(authorUtils.getGitlabAuthors(), GitlabAuthor.GitlabAuthorType)
            updateAuthorIndexes()
        }
    }

    def updateArtifacts(): Unit = {
        if (!ModelUtils.isArtifactCacheUpdated()) {
            storeObjects(artifactUtils.getFiles(), FileArtifact.FileArtifactType)
            storeObjects(artifactUtils.getPipelineReports(), PipelineReportArtifact.PipelineReportArtifactType)
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
        updateIndexes(ObjectTypes.OriginTypes.toSeq)
    }

    def updateEventIndexes(): Unit = {
        updateIndexes(ObjectTypes.EventTypes.toSeq)
    }

    def updateAuthorIndexes(): Unit = {
        updateIndexes(ObjectTypes.AuthorTypes.toSeq)
    }

    def updateArtifactIndexes(): Unit = {
        updateIndexes(ObjectTypes.ArtifactTypes.toSeq)
    }

    def updateTargetCache(targetType: String): Unit = {
        targetType match {
            case Event.EventType => updateEvents()
            case Origin.OriginType => updateOrigins()
            case Artifact.ArtifactType => updateArtifacts()
            case Author.AuthorType => updateAuthors()
            case ObjectTypes.TargetTypeAll =>
                ObjectTypes.objectTypes.keySet.foreach(target => updateTargetCache(target))
            case _ =>
        }
    }

    def getReadConfig(collectionName: String): ReadConfig = {
        ModelUtils.getReadConfig(sparkSession, collectionName)
    }

    def loadMongoData[DataSchema <: Product: TypeTag](collectionName: String): DataFrame = {
        MongoSpark
            .load[DataSchema](sparkSession, getReadConfig(collectionName))
    }

    private def storeObjects[ObjectType](dataset: Dataset[ObjectType], collectionName: String): Unit = {
        GeneralQueryUtils.storeObjects(sparkSession, dataset, collectionName)
    }
}

object ModelUtils {
    def isOriginCacheUpdated(): Boolean = {
        isTargetCacheUpdated(Origin.OriginType)
    }

    def isEventCacheUpdated(): Boolean = {
        isTargetCacheUpdated(Event.EventType)
    }

    def isAuthorCacheUpdated(): Boolean = {
        isTargetCacheUpdated(Author.AuthorType)
    }

    def isArtifactCacheUpdated(): Boolean = {
        isTargetCacheUpdated(Artifact.ArtifactType)
    }

    def isTargetCacheUpdated(targetType: String): Boolean = {
        (
            targetType match {
                case Event.EventType => Some(ObjectTypes.EventTypes)
                case Origin.OriginType => Some(ObjectTypes.OriginTypes)
                case Artifact.ArtifactType => Some(ObjectTypes.ArtifactTypes)
                case Author.AuthorType => Some(ObjectTypes.AuthorTypes)
                case _ => None
            }
        ) match {
            case Some(objectTypes: Set[String]) =>
                objectTypes.forall(objectType => GeneralQueryUtils.isCacheUpdated(objectType))
            case None => false
        }
    }

    def getReadConfig(sparkSession: SparkSession, collectionName: String): ReadConfig = {
        ConfigUtils.getReadConfig(
            sparkSession,
            AdapterValues.gitlabDatabaseName,
            collectionName
        )
    }

}
