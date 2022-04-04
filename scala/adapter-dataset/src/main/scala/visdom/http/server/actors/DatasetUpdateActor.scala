package visdom.http.server.actors

import visdom.adapters.dataset.usecases.DatasetCacheUpdater
import visdom.adapters.general.usecases.CacheUpdater


class DatasetUpdateActor extends UpdateActor {
    override val cacheUpdaterClass: Class[_ <: CacheUpdater] = classOf[DatasetCacheUpdater]
}
