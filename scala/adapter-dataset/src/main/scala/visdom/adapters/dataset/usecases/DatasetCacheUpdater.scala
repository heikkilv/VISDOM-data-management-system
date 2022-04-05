package visdom.adapters.dataset.usecases

import org.apache.spark.sql.SparkSession
import visdom.adapters.general.usecases.CacheUpdater
import visdom.adapters.options.CacheQueryOptions
import visdom.adapters.utils.DatasetModelUtils


class DatasetCacheUpdater(queryOptions: CacheQueryOptions, sparkSession: SparkSession)
extends CacheUpdater(queryOptions, sparkSession) {
    override def runQuery(): Unit = {
        new DatasetModelUtils(sparkSession).updateTargetCache(queryOptions.targetType)
    }
}
