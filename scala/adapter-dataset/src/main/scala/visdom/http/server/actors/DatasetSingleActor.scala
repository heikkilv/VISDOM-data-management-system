package visdom.http.server.actors

import visdom.adapters.dataset.usecases.DatasetSingleQuery
import visdom.adapters.general.usecases.SingleQuery
import visdom.adapters.queries.IncludesQueryCode


class DatasetSingleActor extends SingleActor {
    override val singleQueryClass: Class[_ <: SingleQuery] = classOf[DatasetSingleQuery]
    override val singleQueryObject: IncludesQueryCode = DatasetSingleQuery
}
