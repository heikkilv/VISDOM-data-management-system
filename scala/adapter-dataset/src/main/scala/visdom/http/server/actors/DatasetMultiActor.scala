package visdom.http.server.actors

import visdom.adapters.dataset.usecases.DatasetMultiQuery
import visdom.adapters.general.usecases.MultiQuery
import visdom.adapters.queries.IncludesQueryCode


class DatasetMultiActor extends MultiActor {
    override val multiQueryClass: Class[_ <: MultiQuery] = classOf[DatasetMultiQuery]
    override val multiQueryObject: IncludesQueryCode = DatasetMultiQuery
}
