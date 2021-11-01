package visdom.json

import java.time.Instant
import java.time.ZonedDateTime
import org.bson.BSONException
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
import scala.collection.JavaConverters.asScalaSetConverter
import spray.json.JsArray
import spray.json.JsBoolean
import spray.json.JsNull
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.utils.FileUtils


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

        def getDoubleOption(key: Any): Option[Double] = {
            document.getOption(key) match {
                case Some(value: BsonValue) => value.isDouble() match {
                    case true => Some(value.asDouble().getValue())
                    case false => None
                }
                case None => None
            }
        }

        def getInstantOption(key: Any): Option[Instant] = {
            document.getOption(key) match {
                case Some(value: BsonValue) => value.isTimestamp() match {
                    case true => Some(Instant.ofEpochMilli(value.asTimestamp().getValue()))
                    case false => None
                }
                case None => None
            }
        }

        def getZonedDateTimeOption(key: Any): Option[ZonedDateTime] = {
            GeneralUtils.toZonedDateTime(document.getStringOption(key))
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

        def getManyStringOption(keys: Any*): Option[Seq[String]] = {
            def getManyStringOptionInternal(keySequence: Seq[Any], values: Seq[String]): Seq[String] = {
                keySequence.headOption match {
                    case Some(headKey: Any) => document.getStringOption(headKey) match {
                        case Some(headValue: String) =>
                            getManyStringOptionInternal(keySequence.drop(1), values ++ Seq(headValue))
                        case None => Seq.empty
                    }
                    case None => values
                }
            }

            val valueSequence: Seq[String] = getManyStringOptionInternal(keys, Seq.empty)
            valueSequence.nonEmpty match {
                case true => Some(valueSequence)
                case false => None
            }
        }

        def appendOption(key: String, optionValue: Option[BsonValue]): BsonDocument = {
            optionValue match {
                case Some(value: BsonValue) => document.append(key, value)
                case None => document
            }
        }

        def transformAttribute(
            key: String,
            transformFunction: (String, BsonValue) => (String, BsonValue)
        ): BsonDocument = {
            key == CommonConstants.Dot match {
                case true => transformAttributes(
                    document.keySet().asScala.toSeq.map(subKey => Seq(subKey)),
                    transformFunction
                )
                case false => document.containsKey(key) match {
                    case true => {
                        val originalValue: BsonValue = document.remove(key)
                        val (newKey: String, newValue: BsonValue) = transformFunction(key, originalValue)
                        document.append(newKey, newValue)
                    }
                    case false => document
                }
            }
        }

        def transformAttribute(
            keySequence: Seq[String],
            transformFunction: (String, BsonValue) => (String, BsonValue)
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
            transformFunction: (String, BsonValue) => (String, BsonValue)
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
                    transformAttributes(attributes, valueTransform(anonymizeValue(_)))
                case None => document
            }
        }

        def toIntMap(): Map[Int, BsonValue] = {
            document
                .keySet()
                .asScala
                .flatMap(keyValue => GeneralUtils.toInt(keyValue))
                .map(intKey => (intKey, document.get(intKey.toString())))
                .toMap
        }

        def toIntStringMap(): Map[Int, String] = {
            document
                .toIntMap()
                .filter({case (_, value) => value.isString()})
                .mapValues(value => value.asString().getValue())
        }

        def toIntDocumentMap(): Map[Int, BsonDocument] = {
            document
                .toIntMap()
                .filter({case (_, value) => value.isDocument()})
                .mapValues(value => value.asDocument())
        }

        def addPrefixToKeys(targetAttributes: Seq[Seq[String]], prefix: String): BsonDocument = {
            transformAttributes(
                // add dot to the end of each sequence to indicate all subattributes
                targetAttributes.map(keySequence => keySequence ++ Seq(CommonConstants.Dot)),
                JsonUtils.keyTransform(key => prefix + key)
            )
        }
    }

    implicit class EnrichedBsonArray(array: BsonArray) {
        def toJson(): String = {
            val documentString: String = BsonDocument().append(CommonConstants.TempString, array).toJson()
            // to get the array, need to remove '{"temp": ' from the start and '}' from the end
            documentString.substring(CommonConstants.TempString.size + 5, documentString.size - 1)
        }
    }

    def toBsonValue[T](value: T): BsonValue = {
        value match {
            case bsonValue: BsonValue => bsonValue
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

    def toBsonArray[T](values: Seq[T]): BsonArray = {
        BsonArray(values.map(value => toBsonValue(value)))
    }

    def valueTransform(transformFunction: BsonValue => BsonValue): (String, BsonValue) => (String, BsonValue) = {
        {
            case (key: String, value: BsonValue) => (key, transformFunction(value))
        }
    }

    def keyTransform(transformFunction: String => String): (String, BsonValue) => (String, BsonValue) = {
        {
            case (key: String, value: BsonValue) => (transformFunction(key), value)
        }
    }

    // scalastyle:off cyclomatic.complexity
    def toJsonValue(value: Any): JsValue = {
        value match {
            case jsValue: JsValue => jsValue
            case stringValue: String => JsString(stringValue)
            case intValue: Int => JsNumber(intValue)
            case longValue: Long => JsNumber(longValue)
            case doubleValue: Double => JsNumber(doubleValue)
            case booleanValue: Boolean => JsBoolean(booleanValue)
            case Some(optionValue) => toJsonValue(optionValue)
            case seqValue: Seq[_] => JsArray(seqValue.map(content => toJsonValue(content)).toList)
            case mapValue: Map[_, _] =>
                JsObject(mapValue.map({case (key, content) => (key.toString(), toJsonValue(content))}))
            case jsonObjectConvertible: JsonObjectConvertible => jsonObjectConvertible.toJsObject()
            case _ => JsNull
        }
    }
    // scalastyle:on cyclomatic.complexity

    def bsonArrayOptionToSeq(arrayOption: Option[BsonArray]): Seq[BsonValue] = {
        arrayOption
            .map(arrayElement => arrayElement.getValues().asScala)
            .getOrElse(Seq.empty)
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
        value.getBsonType() match {
            case BsonType.ARRAY => BsonArray(
                value
                    .asArray()
                    .getValues
                    .asScala.map(originalValue => anonymizeValue(originalValue))
            )
            case BsonType.STRING => {
                val stringValue: String = value.asString().getValue()
                stringValue.nonEmpty match {
                    case true => toBsonValue(GeneralUtils.getHash(stringValue))
                    case false => value  // empty string are not hashed
                }
            }
            case BsonType.INT32 => toBsonValue(GeneralUtils.getHash(value.asInt32().getValue()))
            case _ => value  // values other than strings or integers are not hashed
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

    def getBsonDocumentFromFile(filename: String): Option[BsonDocument] = {
        FileUtils.readTextFile(filename) match {
            case Some(fileContents: String) => {
                try {
                    Some(org.bson.BsonDocument.parse(fileContents))
                }
                catch {
                    case error: BSONException => {
                        println(s"BSON parsing error: ${error}")
                        None
                    }
                }
            }
            case None => None
        }
    }

    def sortJs(jsonValue: JsValue, recursive: Boolean): JsValue = {
        jsonValue match {
            case JsObject(fields: Map[String, JsValue]) => JsObject(
                fields
                    .toList
                    .sortBy({case (key, _) => key})
                    .map({
                        case (key, value) => (
                            key,
                            recursive match {
                                case true => sortJs(value, recursive)
                                case false => value
                            }
                        )
                    })
            )
            case JsArray(array: Vector[JsValue]) => recursive match {
                case true => JsArray(array.map(element => sortJs(element, recursive)))
                case false => JsArray(array)
            }
            case _ => jsonValue
        }
    }

    def sortJs(jsonValue: JsValue): JsValue = {
        sortJs(jsonValue, true)
    }

    implicit class EnrichedJsObject(jsObject: JsObject) {
        def sort(): JsObject = {
            sortJs(jsObject) match {
                case JsObject(fields: Map[String,JsValue]) => JsObject(fields)
                case _ => jsObject
            }
        }

        def sort(recursive: Boolean): JsObject = {
            sortJs(jsObject, recursive) match {
                case JsObject(fields: Map[String,JsValue]) => JsObject(fields)
                case _ => jsObject
            }
        }

        def removeNulls(): JsObject = {
            def constructJsObject(fields: Map[String, JsValue], nonNullFields: Map[String, JsValue]): JsObject = {
                fields.headOption match {
                    case Some((key: String, value: JsValue)) => {
                        val nextNonNullFields = value match {
                            case JsNull => nonNullFields
                            case _ => nonNullFields ++ Map(key -> value)
                        }
                        constructJsObject(fields.drop(1), nextNonNullFields)
                    }
                    case None => JsObject(nonNullFields)
                }
            }

            constructJsObject(jsObject.fields, Map.empty)
        }
    }
}
