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
import visdom.adapters.DefaultAdapterValues
import visdom.adapters.general.AdapterValues
import visdom.adapters.general.model.authors.AplusAuthor
import visdom.adapters.general.model.authors.CommitAuthor
import visdom.adapters.general.model.authors.GitlabAuthor
import visdom.adapters.general.model.artifacts.CoursePointsArtifact
import visdom.adapters.general.model.artifacts.ExercisePointsArtifact
import visdom.adapters.general.model.artifacts.FileArtifact
import visdom.adapters.general.model.artifacts.ModulePointsArtifact
import visdom.adapters.general.model.artifacts.PipelineReportArtifact
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.events.SubmissionEvent
import visdom.adapters.general.model.metadata.CourseMetadata
import visdom.adapters.general.model.metadata.ExerciseMetadata
import visdom.adapters.general.model.metadata.ModuleMetadata
import visdom.adapters.general.model.origins.AplusOrigin
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.schemas.CommitSimpleSchema
import visdom.adapters.general.schemas.CourseSchema
import visdom.adapters.general.schemas.ExerciseSchema
import visdom.adapters.general.schemas.ExerciseAdditionalSchema
import visdom.adapters.general.schemas.GitlabEventSchema
import visdom.adapters.general.schemas.ModuleMetadataOtherSchema
import visdom.adapters.general.schemas.ModuleSchema
import visdom.adapters.general.schemas.PipelineJobSchema
import visdom.adapters.general.schemas.PipelineSchema
import visdom.adapters.general.schemas.PointsSchema
import visdom.adapters.general.schemas.SubmissionSchema
import visdom.adapters.options.ObjectTypesTrait
import visdom.adapters.options.ObjectTypes
import visdom.database.mongodb.MongoConnection
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.CommonConstants
import visdom.utils.SnakeCaseConstants


// scalastyle:off number.of.methods
class ModelUtils(sparkSession: SparkSession) {
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newSequenceEncoder

    val objectTypes: ObjectTypesTrait = ObjectTypes
    val modelUtilsObject: ModelUtilsTrait = ModelUtils

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

    def getCourseIdMap(): Map[Int, Map[Int, Seq[Int]]] = {
        val moduleExercisesMap = getModuleSchemas()
            .map(
                module => (
                    module.id,
                    module._links.map(links => links.exercises.getOrElse(Seq.empty)).getOrElse(Seq.empty)
                )
            )
            .collect()
            .toMap

        getCourseSchemas()
            .map(
                course => (
                    course.id,
                    course._links.map(links => links.modules.getOrElse(Seq.empty)).getOrElse(Seq.empty)
                )
            )
            .map({
                case (courseId, moduleIds) => (
                    courseId,
                    moduleIds.map(moduleId => (moduleId, moduleExercisesMap.getOrElse(moduleId, Seq.empty)))
                )
            })
            .collect()
            .toMap
            .map({case (courseId, modules) => (courseId, modules.toMap)})
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

    def getModuleSchemas(): Dataset[ModuleSchema] = {
        loadMongoDataAplus[ModuleSchema](MongoConstants.CollectionModules)
            .flatMap(row => ModuleSchema.fromRow(row))
            .persist(StorageLevel.MEMORY_ONLY)
    }

    def getModuleDatesMap(): Map[Int, (Option[String], Option[String])] = {
        getModuleSchemas()
            .map(
                schema => (
                    schema.id,
                    schema.metadata.other match {
                        case Some(otherSchema: ModuleMetadataOtherSchema) => (
                            Some(otherSchema.start_date),
                            Some(otherSchema.end_date)
                        )
                        case None => (None, None)
                    }
                )
            )
            .collect()
            .toMap
    }

    def getExerciseSchemas(): Dataset[ExerciseSchema] = {
        loadMongoDataAplus[ExerciseSchema](MongoConstants.CollectionExercises)
            .flatMap(row => ExerciseSchema.fromRow(row))
            .persist(StorageLevel.MEMORY_ONLY)
    }

    def getExerciseAdditionalMap(): Map[Int, ExerciseAdditionalSchema] = {
        val moduleDatesMap: Map[Int, (Option[String], Option[String])] = getModuleDatesMap()
        val points: Dataset[PointsSchema] = getPointsSchemas()

        points.isEmpty match {
            case true => Map.empty
            case false =>
                points
                    .head()
                    .modules
                    .map(
                        module => module.exercises.map(
                            exercise => (
                                exercise.id,
                                (
                                    exercise.difficulty,
                                    exercise.points_to_pass,
                                    moduleDatesMap.getOrElse(module.id, (None, None))
                                )
                            )
                        )
                    )
                    .flatten
                    .map({case (exerciseId, (difficulty, pointsToPass, (startDate, endDate))) => (
                        exerciseId,
                        ExerciseAdditionalSchema(
                            difficulty = Some(difficulty),
                            points_to_pass = Some(pointsToPass),
                            start_date = startDate,
                            end_date = endDate
                        )
                    )})
                    .toMap
        }
    }

    def getSubmissionSchemas(): Dataset[SubmissionSchema] = {
        loadMongoDataAplus[SubmissionSchema](MongoConstants.CollectionSubmissions)
            .flatMap(row => SubmissionSchema.fromRow(row))
            .persist(StorageLevel.MEMORY_ONLY)
    }

    def getExerciseGitMap(): Map[Int, String] = {
        getExerciseSchemas()
            .flatMap(
                exercise => exercise.metadata.other.map(
                    other => (exercise.id, other.path)
                )
            )
            .collect()
            .toMap
    }

    def updateOrigins(): Unit = {
        if (!modelUtilsObject.isOriginCacheUpdated()) {
            storeObjects(originUtils.getGitlabOrigins(), GitlabOrigin.GitlabOriginType)
            storeObjects(originUtils.getAplusOrigins(), AplusOrigin.AplusOriginType)
            updateOriginsIndexes()
        }
    }

    def updateEvents(): Unit = {
        if (!modelUtilsObject.isEventCacheUpdated()) {
            storeObjects(eventUtils.getCommits(), CommitEvent.CommitEventType)
            storeObjects(eventUtils.getPipelines(), PipelineEvent.PipelineEventType)
            storeObjects(eventUtils.getPipelineJobs(), PipelineJobEvent.PipelineJobEventType)
            storeObjects(eventUtils.getSubmissions(), SubmissionEvent.SubmissionEventType)
            updateEventIndexes()
        }
    }

    def updateAuthors(): Unit = {
        if (!modelUtilsObject.isAuthorCacheUpdated()) {
            storeObjects(authorUtils.getCommitAuthors(), CommitAuthor.CommitAuthorType)
            storeObjects(authorUtils.getGitlabAuthors(), GitlabAuthor.GitlabAuthorType)
            storeObjects(authorUtils.getAplusAuthors(), AplusAuthor.AplusAuthorType)
            updateAuthorIndexes()
        }
    }

    def updateArtifacts(): Unit = {
        if (!modelUtilsObject.isArtifactCacheUpdated()) {
            storeObjects(artifactUtils.getFiles(), FileArtifact.FileArtifactType)
            storeObjects(artifactUtils.getPipelineReports(), PipelineReportArtifact.PipelineReportArtifactType)
            storeObjects(artifactUtils.getCoursePoints(), CoursePointsArtifact.CoursePointsArtifactType)
            storeObjects(artifactUtils.getModulePoints(), ModulePointsArtifact.ModulePointsArtifactType)
            storeObjects(artifactUtils.getExercisePoints(), ExercisePointsArtifact.ExercisePointsArtifactType)
            updateArtifactIndexes()
        }
    }

    def updateMetadata(): Unit = {
        if (!modelUtilsObject.isMetadataCacheUpdated()) {
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
        updateIndexes(objectTypes.OriginTypes.toSeq)
    }

    def updateEventIndexes(): Unit = {
        updateIndexes(objectTypes.EventTypes.toSeq)
    }

    def updateAuthorIndexes(): Unit = {
        updateIndexes(objectTypes.AuthorTypes.toSeq)
    }

    def updateArtifactIndexes(): Unit = {
        updateIndexes(objectTypes.ArtifactTypes.toSeq)
    }

    def updateMetadataIndexes(): Unit = {
        updateIndexes(objectTypes.MetadataTypes.toSeq)
    }

    def updateTargetCache(targetType: String): Unit = {
        targetType match {
            case objectTypes.TargetTypeEvent => updateEvents()
            case objectTypes.TargetTypeOrigin => updateOrigins()
            case objectTypes.TargetTypeArtifact => updateArtifacts()
            case objectTypes.TargetTypeAuthor => updateAuthors()
            case objectTypes.TargetTypeMetadata => updateMetadata()
            case objectTypes.TargetTypeAll =>
                objectTypes.objectTypes.keySet.foreach(target => updateTargetCache(target))
            case _ =>
        }

        // clear the memory cache after any update attempt for the Mongo cache
        DefaultAdapterValues.cache.clearCache()
    }

    def getReadConfigGitlab(collectionName: String): ReadConfig = {
        modelUtilsObject.getReadConfigGitlab(sparkSession, collectionName)
    }

    def getReadConfigAplus(collectionName: String): ReadConfig = {
        modelUtilsObject.getReadConfigAplus(sparkSession, collectionName)
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

object ModelUtils extends ModelUtilsTrait
// scalastyle:on number.of.methods
