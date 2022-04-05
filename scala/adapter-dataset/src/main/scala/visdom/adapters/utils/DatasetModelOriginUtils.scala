package visdom.adapters.utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.results.ProjectOriginResult
import visdom.adapters.dataset.results.ProjectOriginResult.ProjectOriginResult
import visdom.adapters.dataset.schemas.ProjectSchema
import visdom.database.mongodb.MongoConstants


class DatasetModelOriginUtils(sparkSession: SparkSession, modelUtils: DatasetModelUtils)
extends ModelOriginUtils(sparkSession, modelUtils) {
    import sparkSession.implicits.newProductEncoder

    def getProjectOrigins(): Dataset[ProjectOriginResult] = {
        val projects: Dataset[Option[ProjectSchema]] =
            modelUtils.loadMongoDataDataset[ProjectSchema](MongoConstants.CollectionProjects)
                .flatMap(row => ProjectSchema.fromRow(row))
                .map(project => Some(project))

        projects
            // add base origin for the dataset with empty project details
            .union(sparkSession.createDataset(Seq[Option[ProjectSchema]](None)))
            .map(project => ProjectOriginResult.fromProjectSchema(AdapterValues.datasetName, project))
    }
}
