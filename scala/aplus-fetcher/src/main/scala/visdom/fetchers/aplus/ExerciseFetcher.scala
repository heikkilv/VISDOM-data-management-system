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
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonValue
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.utils.APlusUtils
import visdom.utils.AttributeConstants
import visdom.utils.CommonConstants


class ExerciseFetcher(options: APlusExerciseOptions)
    extends APlusDataHandler(options) {

    def getFetcherType(): String = APlusConstants.FetcherTypeExercises
    def getCollectionName(): String = MongoConstants.CollectionExercises
    def usePagination(): Boolean = false

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourseId -> options.courseId,
            APlusConstants.AttributeModuleId -> options.moduleId,
            APlusConstants.AttributeParseNames -> options.parseNames
        ).appendOption(
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
        val detailedDocument: BsonDocument = options.exerciseId match {
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

        val parsedDocument: BsonDocument = options.parseNames match {
            case true => APlusUtils.parseDocument(detailedDocument, getParsableAttributes())
            case false => detailedDocument
        }

        addIdentifierAttributes(parsedDocument)
            .append(AttributeConstants.AttributeMetadata, getMetadata())
            .append(AttributeConstants.AttributeLinks, getLinkData())
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
                )
            ).asJava
        )
    }

    private def getLinkData(): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourses -> options.courseId,
            APlusConstants.AttributeModules -> options.moduleId
        )
    }

    def getParsableAttributes(): Seq[String] = {
        Seq(
            APlusConstants.AttributeDisplayName,
            APlusConstants.AttributeHierarchicalName,
            APlusConstants.AttributeName
        )
    }
}
