// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset.usecases

import org.apache.spark.sql.SparkSession
import visdom.adapters.dataset.AdapterValues
import visdom.adapters.general.usecases.CacheUpdater
import visdom.adapters.options.CacheQueryOptions
import visdom.adapters.utils.DatasetModelUtils


class DatasetCacheUpdater(queryOptions: CacheQueryOptions, sparkSession: SparkSession)
extends CacheUpdater(queryOptions, sparkSession) {
    override def runQuery(): Unit = {
        new DatasetModelUtils(sparkSession, AdapterValues.cache, AdapterValues.generalQueryUtils)
            .updateTargetCache(queryOptions.targetType)
    }
}
