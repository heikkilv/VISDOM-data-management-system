package visdom.utils

import org.bson.BsonType.STRING
import org.bson.BsonValue
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonString
import scala.collection.JavaConverters.asScalaBufferConverter
import visdom.fetchers.aplus.APlusConstants
import visdom.fetchers.aplus.FetcherValues
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonArray
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
        document.transformAttributes(attributes, JsonUtils.valueTransform(nameStringTransformer(_)))
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
                    JsonUtils.valueTransform(courseNameTransformer(_, languages))
                )
            case None => courseDocument
        }
    }

    def getParsedGitAnswerCaseHttp(answerParts: Array[String]): Option[(String, String)] = {
        // expected format: ["https", "host_name/project_name.git"]

        def parseAddress(addressParts: Array[String]): Option[(String, String)] = {
            // NOTE: addressParts.size >= 2
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

        answerParts.size match {
            case 2 => {
                val addressParts: Array[String] = answerParts.last.split(CommonConstants.Slash)
                addressParts.size match {
                    case n: Int if n >= 2 => parseAddress(addressParts)
                    case _ => None  // the project name part was missing
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
            case None => BsonDocument(APlusConstants.AttributeRaw -> answer)
        }
    }

    def getParsedGitAnswer(bsonAnswer: BsonValue): BsonValue = {
        bsonAnswer.isString() match {
            case true => getParsedGitAnswer(bsonAnswer.asString().getValue())
            case false => bsonAnswer
        }
    }

    def parseDoubleArrayAttribute(document: BsonDocument, attribute: String): BsonDocument = {
        document.transformAttribute(
            attribute,
            JsonUtils.valueTransform(JsonUtils.transformDoubleArray(_))
        )
    }

    def parseGitAnswer(document: BsonDocument): BsonDocument = {
        document.transformAttribute(
            Seq(APlusConstants.AttributeSubmissionData, CommonConstants.Git),
            JsonUtils.valueTransform(getParsedGitAnswer(_))
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

    def divideProjectNames(projectNames: Seq[String]): Seq[Seq[String]] = {
        def divideProjectNamesInternal(names: Seq[String], dividedSequence: Seq[Seq[String]]): Seq[Seq[String]] = {
            if (names.isEmpty) {
                dividedSequence
            }
            else if (names.size == 1 || names.mkString(CommonConstants.Comma).size <= HttpConstants.MaxUriLength) {
                dividedSequence ++ Seq(names)
            }
            else {
                val (names1, names2): (Seq[String], Seq[String]) = names.splitAt(names.size / 2)
                divideProjectNamesInternal(names2, divideProjectNamesInternal(names1, dividedSequence))
            }
        }

        divideProjectNamesInternal(projectNames, Seq.empty)
    }

    def makeGitlabFetcherTestQuery(fetcherAddress: String): Boolean = {
        HttpUtils.getRequestDocument(
            request = HttpUtils.getSimpleRequest(
                Seq(fetcherAddress, HttpConstants.PathInfo).mkString(CommonConstants.Slash)
            ),
            expectedStatusCode = HttpConstants.StatusCodeOk
        ) match {
            case Some(_) => true
            case None => false
        }
    }

    private def handleProjectsDocument(fetcherAddress: String, projectsDocument: BsonDocument): Unit = {
        projectsDocument.getArrayOption(AttributeConstants.Allowed) match {
            case Some(allowedProjects: BsonArray) => {
                println(
                    s"GitLab fetcher at ${fetcherAddress} allowed data fetching for " +
                    s"${allowedProjects.size()} projects"
                )
            }
            case None => println(s"No allowed projects found in the response from ${fetcherAddress}")
        }

        val disallowedProjects = (
            JsonUtils.bsonArrayOptionToSeq(projectsDocument.getArrayOption(AttributeConstants.Unauthorized)) ++
            JsonUtils.bsonArrayOptionToSeq(projectsDocument.getArrayOption(AttributeConstants.NotFound))
        )
            .map(x =>
                x.isString() match {
                    case true => Some(x.asString().getValue())
                    case false => None
                }
            )
            .flatten
        disallowedProjects.size match {
            case n if n > 0 => println(s"The disallowed projects: ${disallowedProjects}")
            case _ =>
        }
    }

    private def makeGitlabFetcherQueryInternal(
        fetcherAddress: String,
        queryOptions: GitlabFetcherQueryOptions
    ): Unit = {
        val lastProject: String = queryOptions.projectNames.lastOption match {
            case Some(projectName: String) => projectName
            case None => CommonConstants.EmptyString
        }
        println(
            s"Sending query to GitLab fetcher at ${fetcherAddress} for " +
            s"${queryOptions.projectNames.size} projects, last project: ${lastProject}"
        )
        val response: Option[BsonDocument] = HttpUtils.getRequestDocument(
            request =
                HttpUtils.getSimpleRequest(
                    Seq(fetcherAddress, HttpConstants.PathMulti).mkString(CommonConstants.Slash)
                )
                    .param(AttributeConstants.ProjectNames, queryOptions.projectNames.mkString(CommonConstants.Comma))
                    .param(AttributeConstants.FilePath, queryOptions.gitLocation.path)
                    .param(AttributeConstants.Recursive, queryOptions.gitLocation.isFolder.toString()),
            expectedStatusCode = HttpConstants.StatusCodeAccepted
        )

        response match {
            case Some(responseDocument: BsonDocument) =>
                responseDocument.getDocumentOption(AttributeConstants.Options) match {
                    case Some(optionsDocument: BsonDocument) =>
                        optionsDocument.getDocumentOption(AttributeConstants.Projects) match {
                            case Some(projectsDocument: BsonDocument) =>
                                handleProjectsDocument(fetcherAddress, projectsDocument)
                            case None => println(s"No projects found in the response from ${fetcherAddress}")
                        }
                    case None => println(s"No options found in the response from ${fetcherAddress}")
                }
            case None => println(s"Did not get accepted response from ${fetcherAddress}")
        }
    }

    def makeGitlabFetcherQuery(fetcherAddress: String, queryOptions: GitlabFetcherQueryOptions): Unit = {
        makeGitlabFetcherTestQuery(fetcherAddress) match {
            case true =>
                FetcherValues.gitlabTaskList.addTasks(
                    divideProjectNames(queryOptions.projectNames)
                        .map(
                            projectNames => (
                                makeGitlabFetcherQueryInternal(fetcherAddress, _),
                                GitlabFetcherQueryOptions(
                                    projectNames = projectNames,
                                    gitLocation = queryOptions.gitLocation
                                )
                            )
                        )
                )
            case false => println(s"Test query to GitLab fetcher at ${fetcherAddress} failed")
        }
    }
}
