package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.results.ProjectArtifactResult
import visdom.adapters.dataset.results.ProjectArtifactResult.JiraIssueArtifactResult
import visdom.adapters.dataset.results.ProjectArtifactResult.SonarMeasuresArtifactResult
import visdom.adapters.dataset.schemas.JiraIssueSchema
import visdom.database.mongodb.MongoConstants


class DatasetModelArtifactUtils(sparkSession: SparkSession, modelUtils: DatasetModelUtils)
extends ModelArtifactUtils(sparkSession, modelUtils) {
    import sparkSession.implicits.newProductEncoder

    def getJiraIssues(): Dataset[JiraIssueArtifactResult] = {
        modelUtils.loadMongoDataDataset[JiraIssueSchema](MongoConstants.CollectionJiraIssues)
            .flatMap(row => JiraIssueSchema.fromRow(row))
            .map(issue => ProjectArtifactResult.fromJiraIssueSchema(issue, AdapterValues.datasetName))
    }

    def getSonarMeasures(): Dataset[SonarMeasuresArtifactResult] = {
        sparkSession.createDataset(Seq.empty)
    }
}
