package visdom.adapters.utils

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import scala.reflect.runtime.universe.TypeTag
import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.model.artifacts.JiraIssueArtifact
import visdom.adapters.dataset.model.artifacts.SonarMeasuresArtifact
import visdom.adapters.dataset.model.authors.UserAuthor
import visdom.adapters.dataset.model.events.ProjectCommitEvent
import visdom.adapters.dataset.model.origins.ProjectOrigin
import visdom.adapters.options.DatasetObjectTypes
import visdom.adapters.options.ObjectTypesTrait
import visdom.spark.ConfigUtils


class DatasetModelUtils(sparkSession: SparkSession)
extends ModelUtils(sparkSession) {
    override val objectTypes: ObjectTypesTrait = DatasetObjectTypes
    override val modelUtilsObject: ModelUtilsTrait = DatasetModelUtils

    override protected val originUtils: DatasetModelOriginUtils = new DatasetModelOriginUtils(sparkSession, this)
    override protected val eventUtils: DatasetModelEventUtils = new DatasetModelEventUtils(sparkSession, this)
    override protected val artifactUtils: DatasetModelArtifactUtils = new DatasetModelArtifactUtils(sparkSession, this)
    override protected val authorUtils: DatasetModelAuthorUtils = new DatasetModelAuthorUtils(sparkSession, this)

    override def updateOrigins(updateIndexes: Boolean): Unit = {
        if (!modelUtilsObject.isOriginCacheUpdated()) {
            super.updateOrigins(false)
            storeObjects(originUtils.getProjectOrigins(), ProjectOrigin.ProjectOriginType)

            if (updateIndexes) {
                updateOriginsIndexes()
            }
        }
    }

    override def updateEvents(updateIndexes: Boolean): Unit = {
        if (!modelUtilsObject.isEventCacheUpdated()) {
            super.updateEvents(false)
            storeObjects(eventUtils.getProjectCommits(), ProjectCommitEvent.ProjectCommitEventType)

            if (updateIndexes) {
                updateEventIndexes()
            }
        }
    }

    override def updateAuthors(updateIndexes: Boolean): Unit = {
        if (!modelUtilsObject.isAuthorCacheUpdated()) {
            super.updateAuthors(false)
            storeObjects(authorUtils.getUserAuthors(), UserAuthor.UserAuthorType)

            if (updateIndexes) {
                updateAuthorIndexes()
            }
        }
    }

    override def updateArtifacts(updateIndexes: Boolean): Unit = {
        if (!modelUtilsObject.isArtifactCacheUpdated()) {
            super.updateArtifacts(false)
            storeObjects(artifactUtils.getJiraIssues(), JiraIssueArtifact.JiraIssueArtifactType)
            storeObjects(artifactUtils.getSonarMeasures(), SonarMeasuresArtifact.SonarMeasuresArtifactType)

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
}
