package visdom.http.server.actors

import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.usecases.DatasetSingleQuery
import visdom.adapters.general.usecases.SingleQuery
import visdom.adapters.queries.IncludesQueryCode
import visdom.utils.QueryUtils


class DatasetSingleActor extends SingleActor {
    override val singleQueryClass: Class[_ <: SingleQuery] = classOf[DatasetSingleQuery]
    override val singleQueryObject: IncludesQueryCode = DatasetSingleQuery
    override val queryUtils: QueryUtils = AdapterValues.queryUtils
}
