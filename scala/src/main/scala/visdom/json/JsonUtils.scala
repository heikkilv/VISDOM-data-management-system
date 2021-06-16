package visdom.json

import java.time.ZonedDateTime
import org.mongodb.scala.bson.Document
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.BsonNull
import org.mongodb.scala.bson.BsonValue
import visdom.fetchers.gitlab.GitlabConstants
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonInt64
import org.mongodb.scala.bson.BsonBoolean
import org.mongodb.scala.bson.BsonDouble


object JsonUtils {
    implicit class EnrichedBsonDocument(document: BsonDocument) {
        def getStringOption(key: Any): Option[String] = {
            document.containsKey(key) match {
                case true => document.get(key).isString() match {
                    case true => Some(document.getString(key).getValue())
                    case false => None
                }
                case false => None
            }
        }

        def appendOption(key: String, optionValue: Option[BsonValue]): BsonDocument = {
            optionValue match {
                case Some(value: BsonValue) => document.append(key, value)
                case None => document
            }
        }
    }

    def toBsonValue[T](value: T): BsonValue = {
        value match {
            case stringValue: String => BsonString(stringValue)
            case intValue: Int => BsonInt32(intValue)
            case longValue: Long => BsonInt64(longValue)
            case doubleValue: Double => BsonDouble(doubleValue)
            case booleanValue: Boolean => BsonBoolean(booleanValue)
            case zonedDateTimeValue: ZonedDateTime => BsonDateTime(
                zonedDateTimeValue.toInstant().toEpochMilli()
            )
            case _ => BsonNull()
        }
    }

    def removeAttribute(document: BsonDocument, attributeName: String): BsonDocument = {
        document.containsKey(attributeName) match {
            case true => {
                // remove method removes the key from the document and returns the removed value
                val _: BsonValue = document.remove(attributeName)
                document
            }
            case false => document
        }
    }
}
