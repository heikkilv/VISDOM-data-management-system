package visdom.adapters.course.output

import spray.json.JsObject
import visdom.adapters.course.schemas.NameSchema
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class NameOutput(
    number: Option[String],
    fi: Option[String],
    en: Option[String],
    raw: String
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.Number -> JsonUtils.toJsonValue(number),
            SnakeCaseConstants.Fi -> JsonUtils.toJsonValue(fi),
            SnakeCaseConstants.En -> JsonUtils.toJsonValue(en),
            SnakeCaseConstants.Raw -> JsonUtils.toJsonValue(raw)
        )
    }
}

object NameOutput {
    def fromNameSchema(nameSchema: NameSchema): NameOutput = {
        NameOutput(
            number = nameSchema.number,
            fi = nameSchema.fi,
            en = nameSchema.en,
            raw = nameSchema.raw
        )
    }
}
