package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.dataset.results.ProjectArtifactResult.UserAuthorResult


class DatasetModelAuthorUtils(sparkSession: SparkSession, modelUtils: DatasetModelUtils)
extends ModelAuthorUtils(sparkSession, modelUtils) {
    import sparkSession.implicits.newProductEncoder

    def getUserAuthors(): Dataset[UserAuthorResult] = {
        sparkSession.createDataset(Seq.empty)
    }
}
