package visdom.utils.json

import io.circe.Json
import io.circe.JsonNumber


abstract class JsonType

final case object JsonObjectType extends JsonType
final case object JsonArrayType extends JsonType
final case object JsonStringType extends JsonType
final case object JsonNumberType extends JsonType
final case object JsonBooleanType extends JsonType
final case object JsonNullType extends JsonType

abstract class JsonNumberValueType

final case object JsonIntType extends JsonNumberValueType
final case object JsonLongType extends JsonNumberValueType
final case object JsonDoubleType extends JsonNumberValueType


object JsonTypes {
    def getJsonType(jsonDocument: Json): JsonType = {
        if (jsonDocument.isObject) {
            JsonObjectType
        }
        else if (jsonDocument.isArray) {
            JsonArrayType
        }
        else if (jsonDocument.isString) {
            JsonStringType
        }
        else if (jsonDocument.isNumber) {
            JsonNumberType
        }
        else if (jsonDocument.isBoolean) {
            JsonBooleanType
        }
        else {
            JsonNullType
        }
    }

    def getJsonNumberValueType(jsonNumber: JsonNumber): JsonNumberValueType = {
        jsonNumber.toInt match {
            case Some(value: Int) => JsonIntType
            case None => jsonNumber.toLong match {
                case Some(value: Long) => JsonLongType
                case None => JsonDoubleType
            }
        }
    }
}
