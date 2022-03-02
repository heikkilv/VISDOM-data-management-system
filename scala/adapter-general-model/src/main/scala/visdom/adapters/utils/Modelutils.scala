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
import visdom.adapters.general.model.artifacts.PipelineReportArtifact
import visdom.adapters.general.model.base.Artifact
import visdom.adapters.general.model.base.Author
import visdom.adapters.general.model.base.Event
import visdom.adapters.general.model.base.Origin
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.model.events.PipelineEvent
import visdom.adapters.general.model.events.PipelineJobEvent
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.model.results.ArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.FileArtifactResult
import visdom.adapters.general.model.results.ArtifactResult.GitlabAuthorResult
import visdom.adapters.general.model.results.ArtifactResult.PipelineReportArtifactResult
import visdom.adapters.general.model.results.EventResult
import visdom.adapters.general.model.results.EventResult.CommitEventResult
import visdom.adapters.general.model.results.EventResult.PipelineEventResult
import visdom.adapters.general.model.results.EventResult.PipelineJobEventResult
import visdom.adapters.general.model.results.OriginResult
import visdom.adapters.general.model.results.OriginResult.GitlabOriginResult
import visdom.adapters.options.ObjectTypes
import visdom.adapters.general.schemas.CommitAuthorSimpleSchema
import visdom.adapters.general.schemas.FileSchema
import visdom.adapters.general.schemas.GitlabAuthorSchema
import visdom.adapters.general.schemas.GitlabProjectInformationSchema
import visdom.adapters.general.schemas.GitlabProjectSchema
import visdom.adapters.general.schemas.GitlabProjectSimpleSchema
import visdom.adapters.general.schemas.PipelineJobSchema
import visdom.adapters.general.schemas.PipelineReportSchema
import visdom.adapters.general.schemas.PipelineSchema
import visdom.database.mongodb.MongoConnection
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.spark.ConfigUtils
import visdom.utils.CommonConstants
import visdom.utils.SnakeCaseConstants


class ModelUtils(sparkSession: SparkSession) {
    import sparkSession.implicits.newProductEncoder
    import sparkSession.implicits.newStringEncoder

    def getCommitSchemas(): Dataset[CommitSchema] = {
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
    }

    def getCommitJobs(): Map[String, Seq[Int]] = {
        getPipelineJobSchemas()
            .map(jobSchema => (jobSchema.id, jobSchema.commit.id))
            .collect()
            .groupBy({case (_, commitId) => commitId})
            .map({
                case (commitId, jobIdArray) => (
                    commitId,
                    jobIdArray.map({case (jobId, _) => jobId}).toSeq
                )
            })
    }

    def getCommits(): Dataset[CommitEventResult] = {
        val commitJobs: Map[String, Seq[Int]] = getCommitJobs()

        getCommitSchemas()
            .map(
                commitSchema =>
                    EventResult.fromCommitSchema(
                        commitSchema,
                        commitJobs.getOrElse(commitSchema.id, Seq.empty)
                    )
            )
    }

    def getPipelineSchemas(): Dataset[PipelineSchema] = {
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
    }

    def getPipelines(): Dataset[PipelineEventResult] = {
        getPipelineSchemas()
            .map(pipelineSchema => EventResult.fromPipelineSchema(pipelineSchema))
    }

    def getPipelineProjectNames(): Map[Int, String] = {
        getPipelineSchemas()
            .map(pipelineSchema => (pipelineSchema.id, pipelineSchema.project_name))
            .collect()
            .toMap
    }

    def getPipelineJobSchemas(): Dataset[PipelineJobSchema] = {
        MongoSpark
            .load[PipelineJobSchema](
                sparkSession,
                ConfigUtils.getReadConfig(
                    sparkSession,
                    AdapterValues.gitlabDatabaseName,
                    MongoConstants.CollectionJobs
                )
            )
            .flatMap(row => PipelineJobSchema.fromRow(row))
    }

    def getPipelineJobs(): Dataset[PipelineJobEventResult] = {
        val pipelineProjectNames: Map[Int, String] = getPipelineProjectNames()

        getPipelineJobSchemas()
            // include only the jobs that have a known pipeline
            .filter(pipelineJob => pipelineProjectNames.keySet.contains(pipelineJob.pipeline.id))
            .map(
                pipelineJobSchema =>
                    EventResult.fromPipelineJobSchema(
                        pipelineJobSchema,
                        pipelineProjectNames.getOrElse(pipelineJobSchema.pipeline.id, CommonConstants.EmptyString)
                    )
            )
    }

    def getPipelineReports(): Dataset[PipelineReportArtifactResult] = {
        val pipelineProjectNames: Map[Int, String] = getPipelineProjectNames()

        MongoSpark
            .load[PipelineReportSchema](
                sparkSession,
                ConfigUtils.getReadConfig(
                    sparkSession,
                    AdapterValues.gitlabDatabaseName,
                    MongoConstants.CollectionPipelineReports
                )
            )
            .flatMap(row => PipelineReportSchema.fromRow(row))
            // include only the reports that have a known pipeline
            .filter(report => pipelineProjectNames.keySet.contains(report.pipeline_id))
            .map(
                reportSchema =>
                    ArtifactResult.fromPipelineReportSchema(
                        reportSchema,
                        pipelineProjectNames.getOrElse(reportSchema.pipeline_id, CommonConstants.EmptyString)
                    )
            )
    }

    def getHostNames(): Dataset[String] = {
        getAllGitlabProjects()
            .map(project => project.host_name)
    }

    def getGitlabProjects(): Dataset[GitlabProjectSimpleSchema] = {
        MongoSpark
            .load[GitlabProjectSchema](
                sparkSession,
                ConfigUtils.getReadConfig(
                    sparkSession,
                    AdapterValues.gitlabDatabaseName,
                    MongoConstants.CollectionProjects
                )
            )
            .flatMap(row => GitlabProjectSchema.fromRow(row))
            .map(projectSchema => GitlabProjectSimpleSchema.fromProjectSchema(projectSchema))
    }

    def getGitlabProjectInformation(collectionName: String): Dataset[GitlabProjectSimpleSchema] = {
        MongoSpark
            .load[GitlabProjectInformationSchema](
                sparkSession,
                ConfigUtils.getReadConfig(
                    sparkSession,
                    AdapterValues.gitlabDatabaseName,
                    collectionName
                )
            )
            .na.drop()
            .distinct()
            .flatMap(row => GitlabProjectInformationSchema.fromRow(row))
            .map(informationSchema => GitlabProjectSimpleSchema.fromProjectInformationSchema(informationSchema))
    }

    def getAllGitlabProjects(): Dataset[GitlabProjectSimpleSchema] = {
        val projects = getGitlabProjects()
        val commitProjects = getGitlabProjectInformation(MongoConstants.CollectionCommits)
        val fileProjects = getGitlabProjectInformation(MongoConstants.CollectionFiles)
        val pipelineProjects = getGitlabProjectInformation(MongoConstants.CollectionPipelines)

        projects
            .union(commitProjects)
            .union(fileProjects)
            .union(pipelineProjects)
            .distinct()
    }

    def getGitlabHostOrigins(): Dataset[GitlabProjectSimpleSchema] = {
        getHostNames()
            .map(
                hostName => GitlabProjectSimpleSchema(
                    project_id = None,
                    project_name = CommonConstants.EmptyString,
                    group_name = CommonConstants.EmptyString,
                    host_name = hostName
                )
            )
    }

    def getGitlabOrigins(): Dataset[GitlabOriginResult] = {
        val projects = getAllGitlabProjects()
            .union(getGitlabHostOrigins())
            .distinct()
            .map(projectSchema => OriginResult.fromGitlabProjectSimpleSchema(projectSchema))

        val projectWithIds: Array[String] =
            projects
                .filter(origin => origin.data.project_id.isDefined)
                .map(origin => origin.id)
                .distinct()
                .collect()

        projects
            .filter(origin => origin.data.project_id.isDefined || !projectWithIds.contains(origin.id))
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
            GeneralQueryUtils.storeObjects(sparkSession, getPipelineJobs(), PipelineJobEvent.PipelineJobEventType)
            updateEventIndexes()
        }
    }

    def updateAuthors(): Unit = {
        if (!ModuleUtils.isAuthorCacheUpdated()) {
            GeneralQueryUtils.storeObjects(sparkSession, getGitlabAuthors(), GitlabAuthor.GitlabAuthorType)
            updateAuthorIndexes()
        }
    }

    def updateArtifacts(): Unit = {
        if (!ModuleUtils.isArtifactCacheUpdated()) {
            GeneralQueryUtils.storeObjects(sparkSession, getFiles(), FileArtifact.FileArtifactType)
            GeneralQueryUtils.storeObjects(sparkSession, getPipelineReports(), PipelineReportArtifact.PipelineReportArtifactType)
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
            case _ =>
        }
    }
}

object ModuleUtils {
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
}
