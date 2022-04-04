package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.dataset.results.ProjectEventResult.ProjectCommitEventResult


class DatasetModelEventUtils(sparkSession: SparkSession, modelUtils: DatasetModelUtils)
extends ModelEventUtils(sparkSession, modelUtils) {
    import sparkSession.implicits.newProductEncoder

    def getProjectCommits(): Dataset[ProjectCommitEventResult] = {
        sparkSession.createDataset(Seq.empty)
    }
}
