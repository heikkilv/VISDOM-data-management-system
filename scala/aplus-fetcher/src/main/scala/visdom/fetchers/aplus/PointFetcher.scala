package visdom.fetchers.aplus

import org.mongodb.scala.bson.BsonDocument
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConstants
import visdom.fetchers.FetcherConstants
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonValue
import visdom.utils.APlusUtils
import visdom.utils.AttributeConstants
import visdom.utils.CheckQuestionUtils
import visdom.utils.CheckQuestionUtils.EnrichedBsonDocumentWithGdpr
import visdom.utils.CommonConstants


class PointFetcher(options: APlusPointOptions)
extends APlusDataHandler(options) {
    private val checkedUsers: Set[Int] = CheckQuestionUtils.getCheckedUsers(options.courseId, options.gdprOptions)

    def getFetcherType(): String = APlusConstants.FetcherTypePoints
    def getCollectionName(): String = MongoConstants.CollectionPoints
    def usePagination(): Boolean = options.userId.isEmpty

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourseId -> options.courseId,
            APlusConstants.AttributeUseAnonymization -> options.useAnonymization,
            APlusConstants.AttributeParseNames -> options.parseNames
        )
        .appendGdprOptions(options.gdprOptions)
        .appendOption(
            APlusConstants.AttributeUserId,
            options.userId.map(idValue => toBsonValue(idValue))
        )
    }

    def getRequest(): HttpRequest = {
        getRequest(options.userId)
    }

    private def getRequest(userId: Option[Int]): HttpRequest = {
        val uriParts: List[String] = userId match {
            case Some(idNumber: Int) => List(
                APlusConstants.PathCourses,
                options.courseId.toString(),
                APlusConstants.PathPoints,
                idNumber.toString()
            )
            case None => List(
                APlusConstants.PathCourses,
                options.courseId.toString(),
                APlusConstants.PathStudents
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
            APlusConstants.AttributeCourseId,
            APlusConstants.AttributeHostName
        )
    }

    override def getHashableAttributes(): Option[Seq[Seq[String]]] = {
        options.useAnonymization match {
            case true => Some(
                Seq(
                    Seq(APlusConstants.AttributeUsername),
                    Seq(APlusConstants.AttributeStudentId),
                    Seq(APlusConstants.AttributeEmail),
                    Seq(APlusConstants.AttributeFullName)
                )
            )
            case false => None
        }
    }

    def responseToDocumentArray(response: HttpResponse[String]): Array[BsonDocument] = {
        options.userId match {
            case Some(_) => {
                // if the response is for the points of one student,
                // it should contain only one JSON object
                HttpUtils.responseToDocumentArrayCaseDocument(response)
            }
            case None => {
                // if the response is for the points of all students in the course,
                // the actual data should be in given as a list of JSON objects under the attribute "results"
                HttpUtils.responseToDocumentArrayCaseAttributeDocument(response, APlusConstants.AttributeResults)
            }
        }
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        val detailedDocument: BsonDocument = options.userId match {
            case Some(_) => document
            case None => getDetailedDocument(document)
        }

        isDataFetchingAllowed(detailedDocument) match {
            case true => {
                val parsedDocumentNames: BsonDocument = options.parseNames match {
                    case true => APlusUtils.parseDocument(detailedDocument, getParsableAttributes())
                    case false => detailedDocument
                }

                addIdentifierAttributes(
                    parsedDocumentNames.addPrefixToKeys(
                        getPrefixAttributes(),
                        FetcherConstants.PointsByDifficultyPrefix
                    )
                )
                    .append(AttributeConstants.Metadata, getMetadata())
            }
            // no data fetching allowed for the given user id => return empty document
            case false => BsonDocument()
        }
    }

    private def getDetailedDocument(document: BsonDocument): BsonDocument = {
        options.userId match {
            case Some(_) => document
            case None => document.getIntOption(AttributeConstants.Id) match {
                case Some(userId: Int) => {
                    HttpUtils.getRequestDocument(getRequest(Some(userId)), HttpConstants.StatusCodeOk) match {
                        case Some(pointDocument: BsonDocument) =>
                            pointDocument.getIntOption(AttributeConstants.Id) match {
                                case Some(_) => pointDocument
                                case None => document  // no id attribute in the response JSON
                            }
                        case None => document  // a problem fetching the detailed document
                    }
                }
                case None => document  // no id attribute in the input document
            }
        }
    }

    private def addIdentifierAttributes(document: BsonDocument): BsonDocument = {
        document
            .append(APlusConstants.AttributeCourseId, toBsonValue(options.courseId))
            .append(APlusConstants.AttributeHostName, toBsonValue(options.hostServer.hostName))
    }

    private def getMetadata(): BsonDocument = {
        getMetadataBase()
            .append(APlusConstants.AttributeParseNames, toBsonValue(options.parseNames))
            .append(APlusConstants.AttributeUseAnonymization, toBsonValue(options.useAnonymization))
            .appendGdprOptions(options.gdprOptions)
    }

    def getParsableAttributes(): Seq[Seq[String]] = {
        Seq(
            Seq(APlusConstants.AttributeModules, APlusConstants.AttributeName),
            Seq(
                APlusConstants.AttributeModules,
                APlusConstants.AttributeExercises,
                APlusConstants.AttributeName
            )
        )
    }

    def getPrefixAttributes(): Seq[Seq[String]] = {
        Seq(
            Seq(APlusConstants.AttributePointsByDifficulty),
            Seq(APlusConstants.AttributeModules, APlusConstants.AttributePointsByDifficulty)
        )
    }

    private def isDataFetchingAllowed(document: BsonDocument): Boolean = {
        document.getIntOption(APlusConstants.AttributeId) match {
            case Some(userId: Int) =>
                checkedUsers.contains(userId) ||
                options.gdprOptions.exerciseId == CheckQuestionUtils.ExerciseIdForNoGdpr
            case None => false
        }
    }
}
