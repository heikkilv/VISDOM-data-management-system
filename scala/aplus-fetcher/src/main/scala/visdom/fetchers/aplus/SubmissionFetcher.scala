package visdom.fetchers.aplus

import org.bson.BsonType.DOCUMENT
import org.bson.BsonType.STRING
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import scala.collection.JavaConverters.asScalaBufferConverter
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonValue
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.utils.APlusUtils
import visdom.utils.AttributeConstants
import visdom.utils.CheckQuestionUtils
import visdom.utils.CheckQuestionUtils.EnrichedBsonDocumentWithGdpr
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


class SubmissionFetcher(options: APlusSubmissionOptions)
    extends APlusDataHandler(options) {

    @SuppressWarnings(Array(WartRemoverConstants.WartsVar))
    private var gitProjects: Map[String, Set[String]] = Map.empty
    def getGitProject(): Map[String, Set[String]] = gitProjects

    private val checkedUsers: Set[Int] = CheckQuestionUtils.getCheckedUsers(options.courseId, options.gdprOptions)

    def getFetcherType(): String = APlusConstants.FetcherTypeSubmissions
    def getCollectionName(): String = MongoConstants.CollectionSubmissions
    def usePagination(): Boolean = options.submissionId.isEmpty

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourseId -> options.courseId,
            APlusConstants.AttributeExerciseId -> options.exerciseId,
            APlusConstants.AttributeUseAnonymization -> options.useAnonymization,
            APlusConstants.AttributeParseGitAnswers -> options.parseGitAnswers,
            APlusConstants.AttributeParseNames -> options.parseNames
        )
        .appendGdprOptions(options.gdprOptions)
        .appendOption(
            APlusConstants.AttributeSubmissionId,
            options.submissionId.map(idValue => toBsonValue(idValue))
        )
    }

    def getRequest(): HttpRequest = {
        getRequest(options.submissionId)
    }

    private def getRequest(submissionId: Option[Int]): HttpRequest = {
        val uriParts: List[String] = submissionId match {
            case Some(idNumber: Int) => List(
                APlusConstants.PathSubmissions,
                idNumber.toString()
            )
            case None => List(
                APlusConstants.PathExercises,
                options.exerciseId.toString(),
                APlusConstants.PathSubmissions
            )
        }
        val uri: String = (
            List(options.hostServer.baseAddress) ++ uriParts
        ).mkString(CommonConstants.Slash) + CommonConstants.Slash

        options.hostServer.modifyRequest(Http(uri))
    }

    override def getIdentifierAttributes(): Array[String] = {
        Array(
            APlusConstants.AttributeId,
            APlusConstants.AttributeHostName
        )
    }

    override def getHashableAttributes(): Option[Seq[Seq[String]]] = {
        options.useAnonymization match {
            case true => Some(
                Seq(
                    Seq(APlusConstants.AttributeSubmitters, APlusConstants.AttributeUsername),
                    Seq(APlusConstants.AttributeSubmitters, APlusConstants.AttributeStudentId),
                    Seq(APlusConstants.AttributeSubmitters, APlusConstants.AttributeEmail),
                    Seq(APlusConstants.AttributeSubmitters, APlusConstants.AttributeFullName),
                    Seq(APlusConstants.AttributeSubmissionData, CommonConstants.Git, APlusConstants.AttributeProjectName),
                    Seq(APlusConstants.AttributeSubmissionData, CommonConstants.Git, APlusConstants.AttributeRaw)
                )
            )
            case false => None
        }
    }

    def responseToDocumentArray(response: HttpResponse[String]): Array[BsonDocument] = {
        options.submissionId match {
            case Some(_) => {
                // if the response is for one submission,
                // it should contain only one JSON object
                HttpUtils.responseToDocumentArrayCaseDocument(response)
            }
            case None => {
                // if the response is for all submissions for an exercise,
                // the actual data should be in given as a list of JSON objects under the attribute "results"
                HttpUtils.responseToDocumentArrayCaseAttributeDocument(response, APlusConstants.AttributeResults)
            }
        }
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        // try to always get the detailed submission information for each submission
        val detailedDocument: BsonDocument = APlusUtils.parseDoubleArrayAttribute(
            getDetailedDocument(document),
            APlusConstants.AttributeSubmissionData
        )

        isDataFetchingAllowed(detailedDocument) match {
            case true => {
                val parsedDocumentGit: BsonDocument = options.parseGitAnswers match {
                    case true => APlusUtils.parseGitAnswer(detailedDocument)
                    case false => detailedDocument
                }
                val parsedDocumentNames: BsonDocument = options.parseNames match {
                    case true => APlusUtils.parseDocument(parsedDocumentGit, getParsableAttributes())
                    case false => parsedDocumentGit
                }

                updateGitProjects(parsedDocumentNames)

                addIdentifierAttributes(parsedDocumentNames)
                    .append(AttributeConstants.AttributeMetadata, getMetadata())
                    .append(AttributeConstants.AttributeLinks, getLinkData())
            }
            // no data fetching allowed for at least one user involved in the submission => return empty document
            case false => BsonDocument()
        }
    }

    private def getDetailedDocument(document: BsonDocument): BsonDocument = {
        options.submissionId match {
            case Some(_) => document
            case None => document.getIntOption(AttributeConstants.AttributeId) match {
                case Some(submissionId: Int) => {
                    HttpUtils.getRequestDocument(
                        getRequest(Some(submissionId)),
                        HttpConstants.StatusCodeOk
                    ) match {
                        case Some(submissionDocument: BsonDocument) =>
                            submissionDocument.getIntOption(AttributeConstants.AttributeId) match {
                                case Some(_) => submissionDocument
                                case None => document
                            }
                        case None => document
                    }
                }
                case None => document
            }
        }
    }

    private def addIdentifierAttributes(document: BsonDocument): BsonDocument = {
        document
            .append(APlusConstants.AttributeHostName, toBsonValue(options.hostServer.hostName))
    }

    private def getMetadata(): BsonDocument = {
        getMetadataBase()
            .append(APlusConstants.AttributeUseAnonymization, toBsonValue(options.useAnonymization))
            .append(APlusConstants.AttributeParseGitAnswers, toBsonValue(options.parseGitAnswers))
            .append(APlusConstants.AttributeParseNames, toBsonValue(options.parseNames))
            .appendGdprOptions(options.gdprOptions)
    }

    def getParsableAttributes(): Seq[Seq[String]] = {
        Seq(
            Seq(APlusConstants.AttributeExercise, APlusConstants.AttributeDisplayName),
            Seq(APlusConstants.AttributeExercise, APlusConstants.AttributeHierarchicalName)
        )
    }

    private def getLinkData(): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourses -> options.courseId,
            APlusConstants.AttributeExercises -> options.exerciseId
        )
    }

    private def checkUserDocument(userDocument: BsonDocument): Boolean = {
        userDocument.getIntOption(APlusConstants.AttributeId) match {
            case Some(userId: Int) =>
                checkedUsers.contains(userId) ||
                options.gdprOptions.exerciseId == CheckQuestionUtils.ExerciseIdForNoGdpr
            case None => false
        }
    }

    private def checkUserDocuments(userDocuments: Seq[BsonDocument]): Boolean = {
        userDocuments.headOption match {
            case Some(userDocument: BsonDocument) =>
                checkUserDocument(userDocument) && checkUserDocuments(userDocuments.drop(1))
            case None => true
        }
    }

    private def isDataFetchingAllowed(document: BsonDocument): Boolean = {
        document.getArrayOption(APlusConstants.AttributeSubmitters) match {
            case Some(submittersArray: BsonArray) => {
                checkUserDocuments(
                    submittersArray
                        .getValues()
                        .asScala
                        .map(
                            userValue => userValue.isDocument() match {
                                case true => userValue.asDocument()
                                case false => BsonDocument()
                            }
                        )
                )
            }
            case None => false
        }
    }

    private def addGitProject(hostName: String, projectName: String): Unit = {
        gitProjects = APlusUtils.appendValueToMapOfSet(gitProjects, hostName, projectName)
    }

    private def updateGitProjects(document: BsonDocument): Unit = {
        (
            document.getDocumentOption(APlusConstants.AttributeSubmissionData) match {
                case Some(submissionData: BsonDocument) => submissionData.getOption(CommonConstants.Git) match {
                    case Some(gitValue: BsonValue) => gitValue.getBsonType() match {
                        case STRING => APlusUtils.getParsedGitAnswerOption(gitValue.asString().getValue())
                        case DOCUMENT =>
                            gitValue
                                .asDocument()
                                .getManyStringOption(
                                    APlusConstants.AttributeHostName,
                                    APlusConstants.AttributeProjectName
                                )
                                .map(targetValues => targetValues.toTuple2)
                        case _ => None  // the git answer given in unexpected format
                    }
                    case None => None  // no git answer found in the submission data
                }
                case None => None  // no submission data found in the document
            }
        ) match {
            case Some((hostName: String, projectName: String)) => addGitProject(hostName, projectName)
            case None =>
        }
    }
}
