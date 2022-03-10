package visdom.adapters.general.usecases

import visdom.adapters.options.BaseQueryOptions
import visdom.adapters.options.CacheQueryOptions
import visdom.adapters.options.MultiQueryOptions
import visdom.adapters.options.ObjectTypes
import visdom.adapters.queries.BaseCacheQuery
import visdom.adapters.queries.BaseSparkQuery
import visdom.adapters.queries.IncludesQueryCode
import visdom.adapters.results.BaseResultValue
import visdom.adapters.utils.GeneralQueryUtils
import visdom.adapters.utils.ModelUtils


class MultiQuery(queryOptions: MultiQueryOptions)
extends BaseCacheQuery(queryOptions) {
    private val dataAttributes: Option[Seq[String]] = queryOptions.dataAttributes
    private val extraAttributes: Seq[String] =
        queryOptions.includedLinks.linkAttributes
            .filter({case (_, isIncluded) => !isIncluded})
            .map({case (attributeName, _) => attributeName})
            .toSeq

    def cacheCheck(): Boolean = {
        ModelUtils.isTargetCacheUpdated(queryOptions.targetType)
    }

    def updateCache(): (Class[_ <: BaseSparkQuery], BaseQueryOptions) = {
        (classOf[CacheUpdater], CacheQueryOptions(queryOptions.targetType))
    }

    def getResults(): Option[BaseResultValue] = {
        queryOptions.objectType match {
            case Some(objectType: String) =>
                Some(GeneralQueryUtils.getCacheResults(objectType, queryOptions, dataAttributes, extraAttributes))
            case None => ObjectTypes.objectTypes.get(queryOptions.targetType) match {
                case Some(objectTypes: Set[String]) =>
                    Some(
                        GeneralQueryUtils.getCacheResults(
                            objectTypes.toSeq,
                            queryOptions,
                            dataAttributes,
                            extraAttributes
                        )
                    )
                case None => None
            }
        }
    }
}

object MultiQuery extends IncludesQueryCode {
    val queryCode: Int = 103
}
