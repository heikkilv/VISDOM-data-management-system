package visdom.adapters.general.usecases

import visdom.adapters.options.CacheQueryOptions
import visdom.adapters.options.ObjectTypes
import visdom.adapters.options.SingleQueryOptions
import visdom.adapters.queries.BaseCacheQuery
import visdom.adapters.queries.BaseSparkQuery
import visdom.adapters.queries.IncludesQueryCode
import visdom.adapters.results.BaseResultValue
import visdom.adapters.utils.GeneralQueryUtils
import visdom.adapters.utils.ModuleUtils
import visdom.utils.CommonConstants


class SingleQuery(queryOptions: SingleQueryOptions)
extends BaseCacheQuery(queryOptions) {
    def cacheCheck(): Boolean = {
        ObjectTypes.getTargetType(queryOptions.objectType) match {
            case Some(targetType: String) => ModuleUtils.isTargetCacheUpdated(targetType)
            case None => false
        }
    }

    def updateCache(): (Class[_ <: BaseSparkQuery], CacheQueryOptions) = {
        (
            classOf[CacheUpdater],
            CacheQueryOptions(
                ObjectTypes.getTargetType(queryOptions.objectType).getOrElse(CommonConstants.EmptyString)
            )
        )
    }

    def getResults(): Option[BaseResultValue] = {
        GeneralQueryUtils.getCacheResult(queryOptions)
    }
}

object SingleQuery extends IncludesQueryCode {
    val queryCode: Int = 102
}
