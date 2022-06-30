// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.actors

import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.usecases.DatasetCacheUpdater
import visdom.adapters.general.usecases.CacheUpdater
import visdom.utils.QueryUtils


class DatasetUpdateActor extends UpdateActor {
    override val cacheUpdaterClass: Class[_ <: CacheUpdater] = classOf[DatasetCacheUpdater]
    override val queryUtils: QueryUtils = AdapterValues.queryUtils
}
