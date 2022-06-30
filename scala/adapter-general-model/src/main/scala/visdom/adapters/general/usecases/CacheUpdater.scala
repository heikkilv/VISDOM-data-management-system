// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.usecases

import org.apache.spark.sql.SparkSession
import visdom.adapters.general.AdapterValues
import visdom.adapters.options.CacheQueryOptions
import visdom.adapters.queries.BaseSparkQuery
import visdom.adapters.utils.ModelUtils


class CacheUpdater(queryOptions: CacheQueryOptions, sparkSession: SparkSession)
extends BaseSparkQuery(queryOptions, sparkSession) {
    override def runQuery(): Unit = {
        new ModelUtils(sparkSession, AdapterValues.cache, AdapterValues.generalQueryUtils)
            .updateTargetCache(queryOptions.targetType)
    }
}
