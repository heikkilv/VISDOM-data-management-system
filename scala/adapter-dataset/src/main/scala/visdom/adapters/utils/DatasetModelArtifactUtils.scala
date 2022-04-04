package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.dataset.results.ProjectArtifactResult.JiraIssueArtifactResult
import visdom.adapters.dataset.results.ProjectArtifactResult.SonarMeasuresArtifactResult


class DatasetModelArtifactUtils(sparkSession: SparkSession, modelUtils: DatasetModelUtils)
extends ModelArtifactUtils(sparkSession, modelUtils) {
    import sparkSession.implicits.newProductEncoder

    def getJiraIssues(): Dataset[JiraIssueArtifactResult] = {
        sparkSession.createDataset(Seq.empty)
    }

    def getSonarMeasures(): Dataset[SonarMeasuresArtifactResult] = {
        sparkSession.createDataset(Seq.empty)
    }
}
