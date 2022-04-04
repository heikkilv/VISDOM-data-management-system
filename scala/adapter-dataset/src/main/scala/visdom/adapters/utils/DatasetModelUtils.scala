package visdom.adapters.utils

import org.apache.spark.sql.SparkSession
import visdom.adapters.options.DatasetObjectTypes
import visdom.adapters.options.ObjectTypesTrait
import visdom.adapters.dataset.model.artifacts.JiraIssueArtifact
import visdom.adapters.dataset.model.artifacts.SonarMeasuresArtifact
import visdom.adapters.dataset.model.authors.UserAuthor
import visdom.adapters.dataset.model.events.ProjectCommitEvent
import visdom.adapters.dataset.model.origins.ProjectOrigin


class DatasetModelUtils(sparkSession: SparkSession)
extends ModelUtils(sparkSession) {
    override val objectTypes: ObjectTypesTrait = DatasetObjectTypes
    override val modelUtilsObject: ModelUtilsTrait = DatasetModelUtils

    override protected val originUtils: DatasetModelOriginUtils = new DatasetModelOriginUtils(sparkSession, this)
    override protected val eventUtils: DatasetModelEventUtils = new DatasetModelEventUtils(sparkSession, this)
    override protected val artifactUtils: DatasetModelArtifactUtils = new DatasetModelArtifactUtils(sparkSession, this)
    override protected val authorUtils: DatasetModelAuthorUtils = new DatasetModelAuthorUtils(sparkSession, this)

    override def updateOrigins(): Unit = {
        if (!modelUtilsObject.isOriginCacheUpdated()) {
            super.updateOrigins()
            storeObjects(originUtils.getProjectOrigins(), ProjectOrigin.ProjectOriginType)
            updateOriginsIndexes()
        }
    }

    override def updateEvents(): Unit = {
        if (!modelUtilsObject.isEventCacheUpdated()) {
            super.updateEvents()
            storeObjects(eventUtils.getProjectCommits(), ProjectCommitEvent.ProjectCommitEventType)
            updateEventIndexes()
        }
    }

    override def updateAuthors(): Unit = {
        if (!modelUtilsObject.isAuthorCacheUpdated()) {
            super.updateAuthors()
            storeObjects(authorUtils.getUserAuthors(), UserAuthor.UserAuthorType)
            updateAuthorIndexes()
        }
    }

    override def updateArtifacts(): Unit = {
        if (!modelUtilsObject.isArtifactCacheUpdated()) {
            super.updateArtifacts()
            storeObjects(artifactUtils.getJiraIssues(), JiraIssueArtifact.JiraIssueArtifactType)
            storeObjects(artifactUtils.getSonarMeasures(), SonarMeasuresArtifact.SonarMeasuresArtifactType)
            updateArtifactIndexes()
        }
    }
}

object DatasetModelUtils extends ModelUtilsTrait
