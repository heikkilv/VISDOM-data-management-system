package visdom.adapters.utils

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.Document
import scala.reflect.runtime.universe.TypeTag
import visdom.adapters.general.AdapterValues
import visdom.adapters.general.model.authors.CommitAuthor
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.artifacts.CoursePointsArtifact
import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.artifacts.PipelineReportArtifact
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.metadata.CourseMetadata
import visdom.adapters.general.model.metadata.ExerciseMetadata
import visdom.adapters.general.model.metadata.ModuleMetadata
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.schemas.CommitSimpleSchema
import visdom.adapters.general.schemas.CourseSchema
import visdom.adapters.general.schemas.GitlabEventSchema
import visdom.adapters.general.schemas.PipelineJobSchema
import visdom.adapters.general.schemas.PipelineSchema
import visdom.adapters.general.schemas.PointsSchema
import visdom.adapters.options.ObjectTypes
import visdom.database.mongodb.MongoConnection
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.CommonConstants
import visdom.spark.ConfigUtils
import visdom.utils.SnakeCaseConstants


class ModelUtils(sparkSession: SparkSession) {
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newSequenceEncoder

    private val originUtils: ModelOriginUtils = new ModelOriginUtils(sparkSession, this)
    private val eventUtils: ModelEventUtils = new ModelEventUtils(sparkSession, this)
    private val artifactUtils: ModelArtifactUtils = new ModelArtifactUtils(sparkSession, this)
    private val authorUtils: ModelAuthorUtils = new ModelAuthorUtils(sparkSession, this)
    private val metadataUtils: ModelMetadataUtils = new ModelMetadataUtils(sparkSession, this)

    def getProjectNameMap(): Map[Int, String] = {
        getPipelineProjectNames() ++
        originUtils.getGitlabProjects()
            .flatMap(schema => schema.project_id match {
                case Some(projectId: Int) => Some(projectId, schema.project_name)
                case None => None
            })
            .persist(StorageLevel.MEMORY_ONLY)
            .collect()
            .toMap
    }

    def getCommitParentMap(): Map[String, Seq[String]] = {
        loadMongoDataGitlab[CommitSimpleSchema](MongoConstants.CollectionCommits)
            .flatMap(row => CommitSimpleSchema.fromRow(row))
            .map(commitSchema => (commitSchema.id, commitSchema.parent_ids))
            .persist(StorageLevel.MEMORY_ONLY)
            .collect()
            .toMap
    }

    def getCommitCommitterMap(): Map[String, String] = {
        loadMongoDataGitlab[CommitSimpleSchema](MongoConstants.CollectionCommits)
            .flatMap(row => CommitSimpleSchema.fromRow(row))
            .map(
                commitSchema => (
                    CommitEvent.getId(commitSchema.host_name, commitSchema.project_name, commitSchema.id),
                    CommitAuthor.getId(GitlabOrigin.getId(commitSchema.host_name), commitSchema.committer_email)
                )
            )
            .persist(StorageLevel.MEMORY_ONLY)
            .collect()
            .toMap
    }

    def getUserCommitMap(): Map[(String, Int), Seq[String]] = {
        val projectNameMap: Map[Int, String] = getProjectNameMap()
        val commitParentMap: Map[String,Seq[String]] = getCommitParentMap()

        loadMongoDataGitlab[GitlabEventSchema](MongoConstants.CollectionEvents)
            .flatMap(row => GitlabEventSchema.fromRow(row))
            .map(
                schema => (
                    schema.host_name,
                    schema.author_id,
                    ModelHelperUtils.getEventCommits(projectNameMap, commitParentMap, schema)
                )
            )
            .groupByKey({case (hostName, userId, _) => (hostName, userId)})
            .mapValues({case (_, _, commitEventIds) => commitEventIds})
            .reduceGroups((first, second) => first ++ second)
            .persist(StorageLevel.MEMORY_ONLY)
            .collect()
            .toMap
    }

    def getUserCommitterMap(): Map[(String, Int), Seq[String]] = {
        val commitCommitterMap: Map[String,String] = getCommitCommitterMap()

        getUserCommitMap
            .map({
                case ((hostName, userId), commitEventIds) => (
                    (hostName, userId),
                    commitEventIds.map(commitEventId => commitCommitterMap.get(commitEventId))
                        .filter(commitEventId => commitEventId.isDefined)
                        .map(commitEventId => commitEventId.getOrElse(CommonConstants.EmptyString))
                        .distinct
                )
            })
    }

    def getPipelineSchemas(): Dataset[PipelineSchema] = {
        loadMongoDataGitlab[PipelineSchema](MongoConstants.CollectionPipelines)
            .flatMap(row => PipelineSchema.fromRow(row))
    }

    def getPipelineJobSchemas(): Dataset[PipelineJobSchema] = {
        loadMongoDataGitlab[PipelineJobSchema](MongoConstants.CollectionJobs)
            .flatMap(row => PipelineJobSchema.fromRow(row))
            .persist(StorageLevel.MEMORY_ONLY)
    }

    def getPipelineProjectNames(): Map[Int, String] = {
        getPipelineSchemas()
            .map(pipelineSchema => (pipelineSchema.id, pipelineSchema.project_name))
            .persist(StorageLevel.MEMORY_ONLY)
            .collect()
            .toMap
    }

    def getPointsSchemas(): Dataset[PointsSchema] = {
        loadMongoDataAplus[PointsSchema](MongoConstants.CollectionPoints)
            .flatMap(row => PointsSchema.fromRow(row))
            .persist(StorageLevel.MEMORY_ONLY)
    }

    def getCourseSchemas(): Dataset[CourseSchema] = {
        loadMongoDataAplus[CourseSchema](MongoConstants.CollectionCourses)
            .flatMap(row => CourseSchema.fromRow(row))
            .persist(StorageLevel.MEMORY_ONLY)
    }

    def updateOrigins(): Unit = {
        if (!ModelUtils.isOriginCacheUpdated()) {
            storeObjects(originUtils.getGitlabOrigins(), GitlabOrigin.GitlabOriginType)
            storeObjects(originUtils.getAplusOrigins(), AplusOrigin.AplusOriginType)
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
            storeObjects(artifactUtils.getCoursePoints(), CoursePointsArtifact.CoursePointsArtifactType)
            updateArtifactIndexes()
        }
    }

    def updateMetadata(): Unit = {
        if (!ModelUtils.isMetadataCacheUpdated()) {
            storeObjects(metadataUtils.getCourseMetadata(), CourseMetadata.CourseMetadataType)
            storeObjects(metadataUtils.getModuleMetadata(), ModuleMetadata.ModuleMetadataType)
            storeObjects(metadataUtils.getExerciseMetadata(), ExerciseMetadata.ExerciseMetadataType)
            updateMetadataIndexes()
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

    def updateMetadataIndexes(): Unit = {
        updateIndexes(ObjectTypes.MetadataTypes.toSeq)
    }

    def updateTargetCache(targetType: String): Unit = {
        targetType match {
            case ObjectTypes.TargetTypeEvent => updateEvents()
            case ObjectTypes.TargetTypeOrigin => updateOrigins()
            case ObjectTypes.TargetTypeArtifact => updateArtifacts()
            case ObjectTypes.TargetTypeAuthor => updateAuthors()
            case ObjectTypes.TargetTypeMetadata => updateMetadata()
            case ObjectTypes.TargetTypeAll =>
                ObjectTypes.objectTypes.keySet.foreach(target => updateTargetCache(target))
            case _ =>
        }
    }

    def getReadConfigGitlab(collectionName: String): ReadConfig = {
        ModelUtils.getReadConfigGitlab(sparkSession, collectionName)
    }

    def getReadConfigAplus(collectionName: String): ReadConfig = {
        ModelUtils.getReadConfigAplus(sparkSession, collectionName)
    }

    def loadMongoDataGitlab[DataSchema <: Product: TypeTag](collectionName: String): DataFrame = {
        MongoSpark
            .load[DataSchema](sparkSession, getReadConfigGitlab(collectionName))
    }

    def loadMongoDataAplus[DataSchema <: Product: TypeTag](collectionName: String): DataFrame = {
        MongoSpark
            .load[DataSchema](sparkSession, getReadConfigAplus(collectionName))
    }

    private def storeObjects[ObjectType](dataset: Dataset[ObjectType], collectionName: String): Unit = {
        GeneralQueryUtils.storeObjects(sparkSession, dataset, collectionName)
    }
}

object ModelUtils {
    def isOriginCacheUpdated(): Boolean = {
        isTargetCacheUpdated(ObjectTypes.TargetTypeOrigin)
    }

    def isEventCacheUpdated(): Boolean = {
        isTargetCacheUpdated(ObjectTypes.TargetTypeEvent)
    }

    def isAuthorCacheUpdated(): Boolean = {
        isTargetCacheUpdated(ObjectTypes.TargetTypeAuthor)
    }

    def isArtifactCacheUpdated(): Boolean = {
        isTargetCacheUpdated(ObjectTypes.TargetTypeArtifact)
    }

    def isMetadataCacheUpdated(): Boolean = {
        isTargetCacheUpdated(ObjectTypes.TargetTypeMetadata)
    }

    def isTargetCacheUpdated(targetType: String): Boolean = {
        ObjectTypes.objectTypes.get(targetType) match {
            case Some(objectTypes: Set[String]) =>
                objectTypes.forall(objectType => GeneralQueryUtils.isCacheUpdated(objectType))
            case None => false
        }
    }

    def getReadConfigGitlab(sparkSession: SparkSession, collectionName: String): ReadConfig = {
        ConfigUtils.getReadConfig(
            sparkSession,
            AdapterValues.gitlabDatabaseName,
            collectionName
        )
    }

    def getReadConfigAplus(sparkSession: SparkSession, collectionName: String): ReadConfig = {
        ConfigUtils.getReadConfig(
            sparkSession,
            AdapterValues.aPlusDatabaseName,
            collectionName
        )
    }
}
