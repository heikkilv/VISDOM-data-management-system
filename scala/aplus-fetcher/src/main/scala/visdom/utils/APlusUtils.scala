package visdom.utils

import org.bson.BsonType.STRING
import org.bson.BsonValue
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonString
import scala.collection.JavaConverters.asScalaBufferConverter
import visdom.fetchers.aplus.APlusConstants
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonValue


object APlusUtils {
    final val NameStringSeparator: Char = '|'
    final val NameStringLanguageSeparator: Char = ':'

    final val CourseNameSeparator: Char = '/'
    final val CodeNameSeparator: Char = ' '

    final val EmptyString: String = ""
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

    def nameStringTransformer(value: BsonValue): BsonValue = {
        value.isString() match {
            case true => {
                BsonDocument(
                    parseNameString(value.asString().getValue())
                        .mapValues(stringValue => toBsonValue(stringValue)
                    )
                )
            }
            case false => value
        }
    }

    def parseDocument(document: BsonDocument, attributes: Seq[Seq[String]]): BsonDocument = {
        document.transformAttributes(attributes, nameStringTransformer(_))
    }

    def parseCourseName(fullCourseName: String, languages: Seq[String]): Map[String, BsonValue] = {
        (
            languages
                .zip(
                    fullCourseName
                        .split(CourseNameSeparator)
                        .map(namePart => namePart.trim.split(CodeNameSeparator))
                        .map(
                            namePart => {
                                val codeAndName: (String, Array[String]) = namePart.size match {
                                    case 1 => (EmptyString, namePart)
                                    case _ => (namePart.head, namePart.drop(1))
                                }
                                 BsonDocument(
                                    CommonConstants.Code -> codeAndName._1,
                                    CommonConstants.Name -> codeAndName._2.mkString(CodeNameSeparator.toString())
                                )
                            }
                        )
                )
                .toMap
        ) + (APlusConstants.AttributeRaw -> BsonString(fullCourseName))
    }

    def getCourseLanguages(courseDocument: BsonDocument): Option[Seq[String]] = {
        courseDocument.getStringOption(APlusConstants.AttributeLanguage) match {
            case Some(languageString: String) => Some(
                languageString
                    .split(NameStringSeparator)
                    .map(language => language.trim)
                    .filter(language => language.size > 0)
            )
            case None => None
        }
    }

    def courseNameTransformer(value: BsonValue, languages: Seq[String]): BsonValue = {
        value.isString() match {
            case true => BsonDocument(parseCourseName(value.asString().getValue(), languages))
            case false => value
        }
    }

    def parseCourseDocument(courseDocument: BsonDocument): BsonDocument = {
        getCourseLanguages(courseDocument) match {
            case Some(languages: Seq[String]) =>
                courseDocument.transformAttributes(
                    Seq(Seq(APlusConstants.AttributeName)),
                    courseNameTransformer(_, languages)
                )
            case None => courseDocument
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

                        // the host name should contain exactly one double dot
                        // and the project name should not contain the string "/-/" or commas
                        if (
                            hostName.count(letter => letter == CommonConstants.DoubleDotChar) != 1 ||
                            projectName.contains(CommonConstants.SlashDashSlash) ||
                            projectName.contains(CommonConstants.Comma)
                        ) {
                            None
                        }
                        else {
                            Some(hostName, cleanProjectName)
                        }
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

    def getParsedGitAnswerOption(answer: String): Option[(String, String)] = {
        val lowerCaseAnswer: String = answer.toLowerCase()

        // the answer is expected to be of two formats:
        //   a) http(s)://host_name/project_name.git
        //   b) git@host_name:project_name.git  (https is assumed in this case)
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

    def getParsedGitAnswer(answer: String): BsonValue = {
        getParsedGitAnswerOption(answer) match {
            case Some((hostName: String, projectName: String)) =>
                BsonDocument(
                    APlusConstants.AttributeHostName -> hostName,
                    APlusConstants.AttributeProjectName -> projectName,
                    APlusConstants.AttributeRaw -> answer
                )
            case None => BsonString(answer)
        }
    }

    def getParsedGitAnswer(bsonAnswer: BsonValue): BsonValue = {
        bsonAnswer.isString() match {
            case true => getParsedGitAnswer(bsonAnswer.asString().getValue())
            case false => bsonAnswer
        }
    }

    def parseDoubleArrayAttribute(document: BsonDocument, attribute: String): BsonDocument = {
        document.transformAttribute(attribute, JsonUtils.transformDoubleArray(_))
    }

    def parseGitAnswer(document: BsonDocument): BsonDocument = {
        document.transformAttribute(
            Seq(APlusConstants.AttributeSubmissionData, CommonConstants.Git),
            getParsedGitAnswer(_)
        )
    }

    def appendValueToMapOfSet[T](origin: Map[String, Set[T]], key: String, value: T): Map[String, Set[T]] = {
        origin.updated(
            key,
            origin.get(key) match {
                case Some(values: Set[T]) => values + value
                case None => Set(value)
            }
        )
    }

    def appendValuesToMapOfSet[T](origin: Map[String, Set[T]], key: String, values: Set[T]): Map[String, Set[T]] = {
        values.headOption match {
            case Some(value) =>
                appendValuesToMapOfSet(
                    appendValueToMapOfSet(origin, key, value),
                    key,
                    values.drop(1)
                )
            case None => origin
        }
    }

    def combinedMapOfSet[T](origin: Map[String, Set[T]], other: Map[String, Set[T]]): Map[String, Set[T]] = {
        other.headOption match {
            case Some((headKey: String, headValues: Set[T])) =>
                combinedMapOfSet(
                    appendValuesToMapOfSet(origin, headKey, headValues),
                    other.drop(1)
                )
            case None => origin
        }
    }
}
