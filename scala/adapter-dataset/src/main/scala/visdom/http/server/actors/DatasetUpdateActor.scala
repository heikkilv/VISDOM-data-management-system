package visdom.http.server.actors

import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.usecases.DatasetCacheUpdater
import visdom.adapters.general.usecases.CacheUpdater
import visdom.utils.QueryUtils


class DatasetUpdateActor extends UpdateActor {
    override val cacheUpdaterClass: Class[_ <: CacheUpdater] = classOf[DatasetCacheUpdater]
    override val queryUtils: QueryUtils = AdapterValues.queryUtils
}
