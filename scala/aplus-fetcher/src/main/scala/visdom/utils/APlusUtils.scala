package visdom.utils

import org.bson.BsonType.STRING
import org.bson.BsonValue
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonString
import visdom.fetchers.aplus.APlusConstants
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

    final val HttpString: String = "http"
    final val GitString: String = ".git"
    final val HostPrefix: String = "://"

    def getParsedGitAnswer(answer: BsonString): BsonValue = {
        val answerString: String = answer.getValue()
        answerString.toLowerCase().startsWith(HttpString) match {
            case true => {
                val answerParts: Array[String] = answerString.split(HostPrefix)
                answerParts.size match {
                    // the answer is expected to of format: http(s)://host_address/project_name.git
                    case 2 => {
                        val addressParts: Array[String] = answerParts.last.split(CommonConstants.Slash)
                        addressParts.size match {
                            case n: Int if n >= 2 => {
                                val hostName: String = List(answerParts.head, addressParts.head).mkString(HostPrefix)
                                val projectName: String = addressParts.tail.mkString(CommonConstants.Slash)
                                val cleanProjectName: String = projectName.endsWith(GitString) match {
                                    case true => projectName.substring(0, projectName.size - GitString.size)
                                    case false => projectName
                                }

                                BsonDocument(
                                    APlusConstants.AttributeGitHostName -> hostName,
                                    APlusConstants.AttributeGitProjectName -> cleanProjectName,
                                    NameStringRawIdentifier -> answerString
                                )
                            }
                            // the project name part was missing
                            case _ => answer
                        }
                    }
                    case _ => answer
                }
            }
            case false => answer
        }
    }

    def parseGitAnswer(document: BsonDocument, attribute: String): BsonDocument = {
        document.getOption(attribute) match {
            case Some(value: BsonValue) => value.getBsonType() match {
                // case ARRAY =>
                case STRING => document.append(attribute, getParsedGitAnswer(value.asString()))
                case _ => document
            }
            case None => document
        }
    }

    def parseGitAnswers(document: BsonDocument, attributes: Seq[String]): BsonDocument = {
        attributes.headOption match {
            case Some(attribute: String) =>
                parseGitAnswers(parseGitAnswer(document, attribute), attributes.drop(1))
            case None => document
        }
    }
}
