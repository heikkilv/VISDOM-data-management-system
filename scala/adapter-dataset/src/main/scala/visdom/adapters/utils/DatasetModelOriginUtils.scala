package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.dataset.results.ProjectOriginResult.ProjectOriginResult


class DatasetModelOriginUtils(sparkSession: SparkSession, modelUtils: DatasetModelUtils)
extends ModelOriginUtils(sparkSession, modelUtils) {
    import sparkSession.implicits.newProductEncoder

    def getProjectOrigins(): Dataset[ProjectOriginResult] = {
        sparkSession.createDataset(Seq.empty)
    }
}
