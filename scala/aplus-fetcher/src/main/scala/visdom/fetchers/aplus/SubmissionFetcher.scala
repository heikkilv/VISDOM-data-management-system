package visdom.fetchers.aplus

import java.time.Instant
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.seqAsJavaListConverter
import org.mongodb.scala.bson.BsonBoolean
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonElement
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonString
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonValue
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.utils.APlusUtils
import visdom.utils.AttributeConstants
import visdom.utils.CheckQuestionUtils
import visdom.utils.CommonConstants
import org.mongodb.scala.bson.BsonArray


class SubmissionFetcher(options: APlusSubmissionOptions)
    extends APlusDataHandler(options) {

    private val checkedUsers: Set[Int] = options.gdprOptions.exerciseId match {
        case CheckQuestionUtils.ExerciseIdForNoGdpr => Set.empty
        case _ => new CheckQuestionUtils(
            courseId = options.courseId,
            exerciseId = options.gdprOptions.exerciseId,
            fieldName = options.gdprOptions.fieldName,
            acceptedAnswer = options.gdprOptions.acceptedAnswer
        ).checkedUsers
    }

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
        .append(
            APlusConstants.AttributeGdprOptions,
            BsonDocument(
                APlusConstants.AttributeExerciseId -> options.gdprOptions.exerciseId,
                APlusConstants.AttributeFieldName -> options.gdprOptions.fieldName,
                APlusConstants.AttributeAcceptedAnswer -> options.gdprOptions.acceptedAnswer
            )
        )
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
            .append(APlusConstants.AttributeHostName, new BsonString(options.hostServer.hostName))
    }

    private def getMetadata(): BsonDocument = {
        new BsonDocument(
            List(
                new BsonElement(
                    APlusConstants.AttributeLastModified,
                    new BsonDateTime(Instant.now().toEpochMilli())
                ),
                new BsonElement(
                    APlusConstants.AttributeApiVersion,
                    new BsonInt32(APlusConstants.APlusApiVersion)
                ),
                new BsonElement(
                    APlusConstants.AttributeUseAnonymization,
                    new BsonBoolean(options.useAnonymization)
                ),
                new BsonElement(
                    APlusConstants.AttributeParseGitAnswers,
                    new BsonBoolean(options.parseGitAnswers)
                ),
                new BsonElement(
                    APlusConstants.AttributeGdprOptions,
                    BsonDocument(
                        APlusConstants.AttributeExerciseId -> options.gdprOptions.exerciseId,
                        APlusConstants.AttributeFieldName -> options.gdprOptions.fieldName,
                        APlusConstants.AttributeAcceptedAnswer -> options.gdprOptions.acceptedAnswer
                    )
                )
            ).asJava
        )
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
            APlusConstants.AttributeExerciseId -> options.exerciseId
        )
    }

    private def checkUserDocument(userDocument: BsonDocument): Boolean = {
        userDocument.getIntOption(APlusConstants.AttributeId) match {
            case Some(userId: Int) => checkedUsers.contains(userId)
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
}
