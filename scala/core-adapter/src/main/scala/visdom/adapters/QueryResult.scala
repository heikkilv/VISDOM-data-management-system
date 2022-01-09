package visdom.adapters

import java.time.Instant
import visdom.adapters.results.Result

final case class QueryResult(
    data: Result,
    timestamp: Instant
)
