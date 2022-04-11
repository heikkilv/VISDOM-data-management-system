package visdom.http.server.actors

import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.usecases.DatasetMultiQuery
import visdom.adapters.general.usecases.MultiQuery
import visdom.adapters.queries.IncludesQueryCode
import visdom.utils.QueryUtils


class DatasetMultiActor extends MultiActor {
    override val multiQueryClass: Class[_ <: MultiQuery] = classOf[DatasetMultiQuery]
    override val multiQueryObject: IncludesQueryCode = DatasetMultiQuery
    override val queryUtils: QueryUtils = AdapterValues.queryUtils
}
