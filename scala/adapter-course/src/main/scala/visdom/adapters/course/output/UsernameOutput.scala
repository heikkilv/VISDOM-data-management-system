package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class UsernameOutput(
    results: Seq[String]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.Results -> JsonUtils.toJsonValue(results)
        )
    }
}
