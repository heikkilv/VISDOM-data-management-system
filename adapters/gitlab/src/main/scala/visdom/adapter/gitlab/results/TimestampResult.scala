package visdom.adapter.gitlab.results

import spray.json.JsArray
import spray.json.JsString
import spray.json.JsValue
import visdom.adapter.gitlab.utils.TimeUtils.toUtcString


final case class TimestampResult(
    project_name: String,
    path: String,
    timestamps: Array[String]
) {
    def toJsonTuple(): (String, String, JsValue) = {
        (
            project_name,
            path,
            JsArray(
                timestamps
                    .map(timestamp => JsString(toUtcString(timestamp)))
                    .toVector
            )
        )
    }
}
