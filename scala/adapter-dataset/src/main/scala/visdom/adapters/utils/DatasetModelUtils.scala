// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.utils

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import scala.reflect.runtime.universe.TypeTag
import visdom.adapters.QueryCache
import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.model.artifacts.JiraIssueArtifact
import visdom.adapters.dataset.model.artifacts.SonarMeasuresArtifact
import visdom.adapters.dataset.model.authors.UserAuthor
import visdom.adapters.dataset.model.events.ProjectCommitEvent
import visdom.adapters.dataset.model.origins.ProjectOrigin
import visdom.adapters.dataset.schemas.CommitSchema
import visdom.adapters.options.DatasetObjectTypes
import visdom.adapters.options.ObjectTypesTrait
import visdom.database.mongodb.MongoConstants
import visdom.spark.ConfigUtils


class DatasetModelUtils(sparkSession: SparkSession, cache: QueryCache, generalQueryUtils: GeneralQueryUtils)
extends ModelUtils(sparkSession, cache, generalQueryUtils) {
    import sparkSession.implicits.newProductEncoder

    override val objectTypes: ObjectTypesTrait = DatasetObjectTypes
    override val modelUtilsObject: ModelUtilsTrait = DatasetModelUtils

    override protected val originUtils: DatasetModelOriginUtils = new DatasetModelOriginUtils(sparkSession, this)
    override protected val eventUtils: DatasetModelEventUtils = new DatasetModelEventUtils(sparkSession, this)
    override protected val artifactUtils: DatasetModelArtifactUtils = new DatasetModelArtifactUtils(sparkSession, this)
    override protected val authorUtils: DatasetModelAuthorUtils = new DatasetModelAuthorUtils(sparkSession, this)

    def getCommitSchemas(): Dataset[CommitSchema] = {
        loadMongoDataDataset[CommitSchema](MongoConstants.CollectionGitCommits)
            .flatMap(row => CommitSchema.fromRow(row))
    }

    override def updateOrigins(updateIndexes: Boolean): Unit = {
        if (!modelUtilsObject.isOriginCacheUpdated()) {
            if (!AdapterValues.onlyDataset) {
                super.updateOrigins(false)
            }

            storeObjects(originUtils.getProjectOrigins(), ProjectOrigin.ProjectOriginType)

            if (updateIndexes) {
                updateOriginsIndexes()
            }
        }
    }

    override def updateEvents(updateIndexes: Boolean): Unit = {
        if (!modelUtilsObject.isEventCacheUpdated()) {
            if (!AdapterValues.onlyDataset) {
                super.updateEvents(false)
            }

            // Note, to avoid "Out of memory" errors, storing is done one project at a time
            eventUtils.storeProjectCommits()

            if (updateIndexes) {
                updateEventIndexes()
            }
        }
    }

    override def updateAuthors(updateIndexes: Boolean): Unit = {
        if (!modelUtilsObject.isAuthorCacheUpdated()) {
            if (!AdapterValues.onlyDataset) {
                super.updateAuthors(false)
            }

            storeObjects(authorUtils.getUserAuthors(), UserAuthor.UserAuthorType)

            if (updateIndexes) {
                updateAuthorIndexes()
            }
        }
    }

    override def updateArtifacts(updateIndexes: Boolean): Unit = {
        if (!modelUtilsObject.isArtifactCacheUpdated()) {
            if (!AdapterValues.onlyDataset) {
                super.updateArtifacts(false)
            }

            storeObjects(artifactUtils.getJiraIssues(), JiraIssueArtifact.JiraIssueArtifactType)
            storeObjects(artifactUtils.getSonarMeasures(), SonarMeasuresArtifact.SonarMeasuresArtifactType)

            if (updateIndexes) {
                updateArtifactIndexes()
            }
        }
    }

    override def updateMetadata(updateIndexes: Boolean): Unit = {
        if (!modelUtilsObject.isMetadataCacheUpdated()) {
            if (!AdapterValues.onlyDataset) {
                super.updateMetadata(false)
            }

            if (updateIndexes) {
                updateArtifactIndexes()
            }
        }
    }

    def getReadConfigDataset(collectionName: String): ReadConfig = {
        ConfigUtils.getReadConfig(
            sparkSession,
            AdapterValues.datasetDatabaseName,
            collectionName
        )
    }

    def loadMongoDataDataset[DataSchema <: Product: TypeTag](collectionName: String): DataFrame = {
        MongoSpark
            .load[DataSchema](sparkSession, getReadConfigDataset(collectionName))
    }
}

object DatasetModelUtils extends ModelUtilsTrait {
    override val objectTypes: ObjectTypesTrait = DatasetObjectTypes

    val generalQueryUtils: GeneralQueryUtils = AdapterValues.generalQueryUtils
}
