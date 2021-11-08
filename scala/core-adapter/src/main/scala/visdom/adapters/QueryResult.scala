package visdom.adapters

import java.time.Instant
import spray.json.JsObject


final case class QueryResult(
    data: JsObject,
    timestamp: Instant
)
