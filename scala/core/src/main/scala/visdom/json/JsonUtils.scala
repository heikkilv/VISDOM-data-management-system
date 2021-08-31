package visdom.json

import java.time.ZonedDateTime
import org.bson.BsonType
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.BsonNull
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonInt64
import org.mongodb.scala.bson.BsonBoolean
import org.mongodb.scala.bson.BsonDouble
import scala.collection.JavaConverters.asScalaBufferConverter
import spray.json.JsBoolean
import spray.json.JsNull
import spray.json.JsNumber
import spray.json.JsString
import spray.json.JsValue
import visdom.utils.GeneralUtils


object JsonUtils {
    implicit class EnrichedBsonDocument(document: BsonDocument) {
        def getOption(key: Any): Option[BsonValue] = {
            document.containsKey(key) match {
                case true => Some(document.get(key))
                case false => None
            }
        }

        def getStringOption(key: Any): Option[String] = {
            document.getOption(key) match {
                case Some(value: BsonValue) => value.isString() match {
                    case true => Some(value.asString().getValue())
                    case false => None
                }
                case None => None
            }
        }

        def getIntOption(key: Any): Option[Int] = {
            document.getOption(key) match {
                case Some(value: BsonValue) => value.isInt32() match {
                    case true => Some(value.asInt32().getValue())
                    case false => None
                }
                case None => None
            }
        }

        def getDocumentOption(key: Any): Option[BsonDocument] = {
            document.getOption(key) match {
                case Some(value: BsonValue) => value.isDocument() match {
                    case true => Some(value.asDocument())
                    case false => None
                }
                case None => None
            }
        }

        def getArrayOption(key: Any): Option[BsonArray] = {
            document.getOption(key) match {
                case Some(value: BsonValue) => value.isArray() match {
                    case true => Some(value.asArray())
                    case false => None
                }
                case None => None
            }
        }

        def appendOption(key: String, optionValue: Option[BsonValue]): BsonDocument = {
            optionValue match {
                case Some(value: BsonValue) => document.append(key, value)
                case None => document
            }
        }

        def transformAttribute(key: String, transformFunction: BsonValue => BsonValue): BsonDocument = {
            document.getOption(key) match {
                case Some(value: BsonValue) => document.append(key, transformFunction(value))
                case None => document
            }
        }

        def transformAttribute(
            keySequence: Seq[String],
            transformFunction: BsonValue => BsonValue
        ): BsonDocument = {
            def transformAttributeInternal(value: BsonValue, tailKeys: Seq[String]): BsonValue = {
                value.getBsonType() match {
                    case BsonType.DOCUMENT =>
                        value
                            .asDocument()
                            .transformAttribute(tailKeys, transformFunction)
                    case BsonType.ARRAY => BsonArray(
                        value
                            .asArray()
                            .getValues()
                            .asScala
                            .map(arrayValue => transformAttributeInternal(arrayValue, tailKeys))
                        )
                    case _ => value
                }
            }

            keySequence.headOption match {
                case Some(key: String) => {
                    val tailKeys: Seq[String] = keySequence.drop(1)
                    tailKeys.isEmpty match {
                        case false => {
                            document.getOption(key) match {
                                case Some(value: BsonValue) =>
                                    document.append(key, transformAttributeInternal(value, tailKeys))
                                case None => document
                            }
                        }
                        case true => document.transformAttribute(key, transformFunction)
                    }
                }
                case None => document
            }
        }

        def transformAttributes(
            targetAttributes: Seq[Seq[String]],
            transformFunction: BsonValue => BsonValue
        ): BsonDocument = {
            targetAttributes.headOption match {
                case Some(attributeSequence: Seq[String]) =>
                    document
                        .transformAttribute(attributeSequence, transformFunction)
                        .transformAttributes(targetAttributes.drop(1), transformFunction)
                case None => document
            }
        }

        def anonymize(hashableAttributes: Option[Seq[Seq[String]]]): BsonDocument = {
            hashableAttributes match {
                case Some(attributes: Seq[Seq[String]]) =>
                    transformAttributes(attributes, JsonUtils.anonymizeValue(_))
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

    def toJsonValue(value: Any): JsValue = {
        value match {
            case jsValue: JsValue => jsValue
            case stringValue: String => JsString(stringValue)
            case intValue: Int => JsNumber(intValue)
            case longValue: Long => JsNumber(longValue)
            case doubleValue: Double => JsNumber(doubleValue)
            case booleanValue: Boolean => JsBoolean(booleanValue)
            case Some(optionValue) => toJsonValue(optionValue)
            case _ => JsNull
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

    def anonymizeValue(value: BsonValue): BsonValue = {
        value.isString() match {
            case true => {
                val stringValue: String = value.asString().getValue()
                stringValue.isEmpty() match {
                    case false => BsonString(GeneralUtils.getHash(stringValue))
                    case true => value
                }
            }
            case false => value
        }
    }

    def arrayToTuple2(bsonArray: BsonArray): Option[(String, BsonValue)] = {
        bsonArray
            .getValues()
            .asScala match {
                case Seq(key: BsonString, target: BsonValue) => Some(key.getValue(), target)
                case _ => None
            }
    }

    def valueToTuple2(bsonValue: BsonValue): Option[(String, BsonValue)] = {
        bsonValue match {
            case bsonArray: BsonArray => arrayToTuple2(bsonArray)
            case _ => None
        }
    }

    def doubleArrayToDocument(value: BsonValue): Option[BsonDocument] = {
        value match {
            case valueArray: BsonArray => {
                    val resultArray: Seq[Option[(String, BsonValue)]] =
                        valueArray
                            .getValues()
                            .asScala
                            .map(subValue => valueToTuple2(subValue))

                    resultArray.contains(None) match {
                        case false => Some(BsonDocument(resultArray.flatten.toMap))
                        case true => None
                    }
                }
            case _ => None
        }
    }

    def transformDoubleArray(value: BsonValue): BsonValue = {
        doubleArrayToDocument(value) match {
            case Some(document: BsonDocument) => document
            case None => value
        }
    }
}
