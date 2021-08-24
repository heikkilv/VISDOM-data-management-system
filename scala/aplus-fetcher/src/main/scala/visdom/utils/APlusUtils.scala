package visdom.utils

import org.bson.BsonType.STRING
import org.bson.BsonValue
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonString
import scala.collection.JavaConverters.asScalaBufferConverter
import visdom.fetchers.aplus.APlusConstants
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonValue
import org.mongodb.scala.bson.BsonArray


object APlusUtils {
    final val NameStringSeparator: Char = '|'
    final val NameStringLanguageSeparator: Char = ':'

    final val GitEndString: String = CommonConstants.Dot + CommonConstants.Git
    final val GitStartString: String = CommonConstants.Git + CommonConstants.AtSign
    final val HostPrefix: String = "://"

    def parseNameString(rawString: String): Map[String, String] = {
        (
            rawString
                .split(NameStringSeparator)
                .map(stringPart => stringPart.trim.split(NameStringLanguageSeparator))
                .map(
                    stringPartArray => stringPartArray.size match {
                        case 1 => (
                            APlusConstants.AttributeNumber,
                            stringPartArray.head
                        )
                        case _ => (
                            stringPartArray.head,
                            stringPartArray.tail.mkString(NameStringLanguageSeparator.toString())
                        )
                    }
                )
                .toMap ++ Map(APlusConstants.AttributeRaw -> rawString)
        ).filter(stringElement => stringElement._1 == APlusConstants.AttributeRaw || stringElement._2.size > 0)
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

    def getParsedGitAnswerCaseHttp(answerParts: Array[String]): Option[(String, String)] = {
        // expected format: ["https", "host_name/project_name.git"]
        answerParts.size match {
            case 2 => {
                val addressParts: Array[String] = answerParts.last.split(CommonConstants.Slash)
                addressParts.size match {
                    case n: Int if n >= 2 => {
                        val hostName: String = List(answerParts.head, addressParts.head).mkString(HostPrefix)
                        val projectName: String = addressParts.tail.mkString(CommonConstants.Slash)
                        val cleanProjectName: String = projectName.endsWith(GitEndString) match {
                            case true => projectName.substring(0, projectName.size - GitEndString.size)
                            case false => projectName
                        }

                        Some(hostName, cleanProjectName)
                    }
                    // the project name part was missing
                    case _ => None
                }
            }
            case _ => None
        }
    }

    def getParsedGitAnswerCaseGit(answerParts: Array[String]): Option[(String, String)] = {
        // expected format: ["git", "host_name:project_name.git"]
        answerParts.size match {
            case 2 => {
                val addressParts: Array[String] = answerParts.last.split(CommonConstants.DoubleDot)
                addressParts.size match {
                    case 2 => {
                        val hostName: String = CommonConstants.Https + HostPrefix + addressParts.head
                        val projectName: String = addressParts.last
                        val cleanProjectName: String = projectName.endsWith(GitEndString) match {
                            case true => projectName.substring(0, projectName.size - GitEndString.size)
                            case false => projectName
                        }

                        Some(hostName, cleanProjectName)
                    }
                    // the project name part was missing
                    case _ => None
                }
            }
            case _ => None
        }
    }

    def getParsedGitAnswer(answer: String): BsonValue = {
        val lowerCaseAnswer: String = answer.toLowerCase()

        // the answer is expected to be of two formats:
        //   a) http(s)://host_name/project_name.git
        //   b) git@host_name:project_name.git  (https is assumed in this case)
        val parsedAnswerOption: Option[(String, String)] = {
            if (lowerCaseAnswer.startsWith(CommonConstants.Http)) {
                getParsedGitAnswerCaseHttp(answer.split(HostPrefix))
            }
            else if (lowerCaseAnswer.startsWith(GitStartString)) {
                getParsedGitAnswerCaseGit(answer.split(CommonConstants.AtSign))
            }
            else {
                None
            }
        }

        parsedAnswerOption match {
            case Some((hostName: String, projectName: String)) =>
                BsonDocument(
                    APlusConstants.AttributeHostName -> hostName,
                    APlusConstants.AttributeProjectName -> projectName,
                    APlusConstants.AttributeRaw -> answer
                )
            case None => BsonString(answer)
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

    def parseDoubleArrayAttribute(document: BsonDocument, attribute: String): BsonDocument = {
        document.getOption(attribute) match {
            case Some(value: BsonValue) => doubleArrayToDocument(value) match {
                case Some(parsedValue: BsonDocument) => document.append(attribute, parsedValue)
                case None => document
            }
            case None => document
        }
    }

    def parseGitAnswer(document: BsonDocument): BsonDocument = {
        document.getDocumentOption(APlusConstants.AttributeSubmissionData) match {
            case Some(submissionData: BsonDocument) => submissionData.getStringOption(CommonConstants.Git) match {
                case Some(gitAnswer: String) => document.append(
                    APlusConstants.AttributeSubmissionData,
                    submissionData.append(
                        CommonConstants.Git,
                        getParsedGitAnswer(gitAnswer)
                    )
                )
                case None => document
            }
            case None => document
        }
    }
}
