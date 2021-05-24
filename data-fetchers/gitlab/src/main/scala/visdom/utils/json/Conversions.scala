package visdom.utils.json

import io.circe.Json
import io.circe.JsonNumber
import io.circe.JsonObject
import org.bson.BsonArray
import org.bson.BsonBoolean
import org.bson.BsonDocument
import org.bson.BsonDouble
import org.bson.BsonElement
import org.bson.BsonInt64
import org.bson.BsonInt32
import org.bson.BsonNull
import org.bson.BsonNumber
import org.bson.BsonString
import org.bson.BsonValue
import org.mongodb.scala.Document
import scala.collection.JavaConverters.seqAsJavaListConverter


object Conversions {

    def jsonObjectsToBson(jsonObjects: Vector[JsonObject]): Vector[Document] = {
        jsonObjects.map(
            jsonObject => Document(
                jsonObjectToBson(jsonObject).asDocument()
            )
        )
    }

    def jsonToBson(jsonDocument: Json): BsonValue = {
        JsonTypes.getJsonType(jsonDocument) match {
            case JsonObjectType => jsonObjectToBson(jsonDocument.asObject.getOrElse(JsonObject.empty))
            case JsonArrayType => jsonArrayToBson(jsonDocument.asArray.getOrElse(Vector()))
            case JsonStringType => new BsonString(jsonDocument.asString.getOrElse(""))
            case JsonNumberType => jsonNumberToBson(
                jsonDocument.asNumber.getOrElse(JsonNumber.fromIntegralStringUnsafe("0"))
            )
            case JsonBooleanType => new BsonBoolean(jsonDocument.asBoolean.getOrElse(false))
            case JsonNullType => new BsonNull()
        }
    }

    def jsonObjectToBson(jsonObject: JsonObject): BsonValue = {
        new BsonDocument(
            jsonObject.toList.map({
                case (key, value) => new BsonElement(key, jsonToBson(value))
            }).asJava
        )
    }

    def jsonArrayToBson(jsonArray: Vector[Json]): BsonValue = {
        new BsonArray(
            jsonArray.map(
                value => jsonToBson(value)
            ).asJava
        )
    }

    def jsonNumberToBson(jsonNumber: JsonNumber): BsonValue = {
        JsonTypes.getJsonNumberValueType(jsonNumber) match {
            case JsonIntType => new BsonInt32(jsonNumber.toInt.getOrElse(0))
            case JsonLongType => new BsonInt64(jsonNumber.toLong.getOrElse(0))
            case JsonDoubleType => new BsonDouble(jsonNumber.toDouble)
        }
    }
}
