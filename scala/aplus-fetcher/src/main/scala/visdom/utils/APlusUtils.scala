package visdom.utils

import org.mongodb.scala.bson.BsonDocument
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonValue


object APlusUtils {
    val NameStringSeparator: Char = '|'
    val NameStringLanguageSeparator: Char = ':'
    val NameStringDefaultIdentifier: String = "number"
    val NameStringRawIdentifier: String = "raw"

    def parseNameString(rawString: String): Map[String, String] = {
        (
            rawString
                .split(NameStringSeparator)
                .map(stringPart => stringPart.trim.split(NameStringLanguageSeparator))
                .map(
                    stringPartArray => stringPartArray.size match {
                        case 1 => (
                            NameStringDefaultIdentifier,
                            stringPartArray.head
                        )
                        case _ => (
                            stringPartArray.head,
                            stringPartArray.tail.mkString(NameStringLanguageSeparator.toString())
                        )
                    }
                )
                .toMap ++ Map(NameStringRawIdentifier -> rawString)
        ).filter(stringElement => stringElement._1 == NameStringRawIdentifier || stringElement._2.size > 0)
    }

    def parseDocument(document: BsonDocument, attributes: Seq[String]): BsonDocument = {
        attributes.headOption match {
            case Some(attribute: String) => parseDocument(
                document.getStringOption(attribute) match {
                    case Some(attributeValue: String) =>
                        document.append(
                            attribute,
                            BsonDocument(
                                parseNameString(attributeValue)
                                    .mapValues(stringValue => toBsonValue(stringValue))
                            )
                        )
                    case None => document
                },
                attributes.drop(1)
            )
            case None => document
        }
    }
}
