package visdom.fetchers.aplus

import java.time.Instant
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import scala.collection.JavaConverters.seqAsJavaListConverter
import org.mongodb.scala.bson.BsonBoolean
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonElement
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.collection.immutable.Document
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonValue
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.utils.APlusUtils
import visdom.utils.AttributeConstants
import visdom.utils.CheckQuestionUtils
import visdom.utils.CommonConstants


class ExerciseFetcher(options: APlusExerciseOptions)
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

    def getFetcherType(): String = APlusConstants.FetcherTypeExercises
    def getCollectionName(): String = MongoConstants.CollectionExercises
    def usePagination(): Boolean = false

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourseId -> options.courseId,
            APlusConstants.AttributeModuleId -> options.moduleId,
            APlusConstants.AttributeParseNames -> options.parseNames,
            APlusConstants.AttributeIncludeSubmissions -> options.includeSubmissions
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
            APlusConstants.AttributeExerciseId,
            options.exerciseId.map(idValue => toBsonValue(idValue))
        )
    }

    def getRequest(): HttpRequest = {
        getRequest(options.exerciseId)
    }

    private def getRequest(exerciseId: Option[Int]): HttpRequest = {
        val uriParts: List[String] = exerciseId match {
            case Some(idNumber: Int) => List(
                APlusConstants.PathExercises,
                idNumber.toString()
            )
            case None => List(
                APlusConstants.PathCourses,
                options.courseId.toString(),
                APlusConstants.PathExercises,
                options.moduleId.toString()
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

    def responseToDocumentArray(response: HttpResponse[String]): Array[BsonDocument] = {
        options.exerciseId match {
            case Some(_) => {
                // if the response is for one exercise,
                // it should contain only one JSON object
                HttpUtils.responseToDocumentArrayCaseDocument(response)
            }
            case None => {
                // if the response is for all exercise in a module,
                // the actual data should be in given as a list of JSON objects under the attribute "exercises"
                HttpUtils.responseToDocumentArrayCaseAttributeDocument(response, APlusConstants.AttributeExercises)
            }
        }
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        // try to always get the detailed exercise information for each exercise
        val detailedDocument: BsonDocument = getDetailedDocument(document)

        val parsedDocument: BsonDocument = options.parseNames match {
            case true => APlusUtils.parseDocument(detailedDocument, getParsableAttributes())
            case false => detailedDocument
        }

        // some exercise form definitions contain translations which have incompatible keys for MongoDB documents
        val cleanedDocument: BsonDocument =
            parsedDocument.getDocumentOption(APlusConstants.AttributeExerciseInfo) match {
                case Some(subDocument: BsonDocument) => parsedDocument.append(
                    APlusConstants.AttributeExerciseInfo,
                    JsonUtils.removeAttribute(subDocument, APlusConstants.AttributeFormI18n)
                )
                case None => parsedDocument
            }

        val submissionIds: Seq[Int] = options.includeSubmissions match {
            case true => fetchSubmissions(cleanedDocument)
            case false => Seq.empty
        }

        addIdentifierAttributes(cleanedDocument)
            .append(AttributeConstants.AttributeMetadata, getMetadata())
            .append(AttributeConstants.AttributeLinks, getLinkData(submissionIds))
    }

    private def getDetailedDocument(document: BsonDocument): BsonDocument = {
        options.exerciseId match {
            case Some(_) => document
            case None => document.getIntOption(AttributeConstants.AttributeId) match {
                case Some(exerciseId: Int) => {
                    HttpUtils.getRequestDocument(
                        getRequest(Some(exerciseId)),
                        HttpConstants.StatusCodeOk
                    ) match {
                        case Some(exerciseDocument: BsonDocument) =>
                            exerciseDocument.getIntOption(AttributeConstants.AttributeId) match {
                                case Some(_) => exerciseDocument
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
                    APlusConstants.AttributeParseNames,
                    new BsonBoolean(options.parseNames)
                ),
                new BsonElement(
                    APlusConstants.AttributeIncludeSubmissions,
                    new BsonBoolean(options.includeSubmissions)
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

    private def getLinkData(submissionIds: Seq[Int]): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourses -> options.courseId,
            APlusConstants.AttributeModules -> options.moduleId,
            APlusConstants.AttributeSubmissions -> submissionIds
        )
    }

    def getParsableAttributes(): Seq[String] = {
        Seq(
            APlusConstants.AttributeDisplayName,
            APlusConstants.AttributeHierarchicalName,
            APlusConstants.AttributeName
        )
    }

    private def fetchSubmissions(document: BsonDocument): Seq[Int] = {
        val exerciseIdOption: Option[Int] = document.getIntOption(APlusConstants.AttributeId)
        val submissionIds: Seq[Int] = exerciseIdOption match {
            case Some(exerciseId: Int) => {
                val submissionFetcher: SubmissionFetcher = new SubmissionFetcher(
                    APlusSubmissionOptions(
                        hostServer = options.hostServer,
                        mongoDatabase = options.mongoDatabase,
                        courseId = options.courseId,
                        exerciseId = exerciseId,
                        submissionId = None,
                        parseGitAnswers = true,
                        useAnonymization = true,
                        gdprOptions = options.gdprOptions
                    )
                )

                submissionFetcher.process() match {
                    case Some(submissionDocuments: Array[Document]) =>
                        submissionDocuments.map(
                            submissionDocument =>
                                submissionDocument
                                    .toBsonDocument
                                    .getIntOption(APlusConstants.AttributeId)
                        ).flatten
                    case None => Seq.empty
                }
            }
            case None => Seq.empty
        }

        println(
            s"Found ${submissionIds.size} submissions to exercise with id ${exerciseIdOption.getOrElse(-1)}"
        )

        submissionIds
    }
}
