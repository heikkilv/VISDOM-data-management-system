package visdom.adapters

import java.time.Instant
import visdom.adapters.results.BaseResultValue

final case class QueryResult(
    data: BaseResultValue,
    timestamp: Instant
)
