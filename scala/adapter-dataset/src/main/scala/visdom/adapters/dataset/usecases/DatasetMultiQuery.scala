package visdom.adapters.dataset.usecases

import visdom.adapters.general.usecases.CacheUpdater
import visdom.adapters.general.usecases.MultiQuery
import visdom.adapters.options.DatasetObjectTypes
import visdom.adapters.options.MultiQueryOptions
import visdom.adapters.options.ObjectTypesTrait
import visdom.adapters.queries.IncludesQueryCode
import visdom.adapters.utils.DatasetModelUtils
import visdom.adapters.utils.ModelUtilsTrait


class DatasetMultiQuery(queryOptions: MultiQueryOptions)
extends MultiQuery(queryOptions) {
    override val objectTypes: ObjectTypesTrait = DatasetObjectTypes
    override val modelUtilsObject: ModelUtilsTrait = DatasetModelUtils
    override val cacheUpdaterClass: Class[_ <: CacheUpdater] = classOf[DatasetCacheUpdater]
}

object DatasetMultiQuery extends IncludesQueryCode {
    val queryCode: Int = 113
}
