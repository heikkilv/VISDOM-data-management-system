package visdom.adapters.queries

import visdom.adapters.options.BaseQueryOptions
import visdom.adapters.results.BaseResultValue


abstract class BaseCacheQuery(queryOptions: BaseQueryOptions) {
    def cacheCheck(): Boolean
    def updateCache(): (Class[_ <: BaseSparkQuery], BaseQueryOptions)
    def getResults(): Option[BaseResultValue]
}

object BaseCacheQuery extends IncludesQueryCode {
    val queryCode: Int = 0
}
