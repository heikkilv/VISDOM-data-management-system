package visdom.fetchers.aplus

import java.time.Instant
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import scala.collection.JavaConverters.seqAsJavaListConverter
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
import visdom.utils.AttributeConstants
import visdom.utils.CommonConstants


class CoursesFetcher(options: APlusCourseOptions)
    extends APlusDataHandler(options) {

    def getFetcherType(): String = APlusConstants.FetcherTypeCourses
    def getCollectionName(): String = MongoConstants.CollectionCourses
    def usePagination(): Boolean = !options.courseId.isDefined

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument().appendOption(
            APlusConstants.AttributeCourseId,
            options.courseId.map(idValue => toBsonValue(idValue))
        )
    }

    def getRequest(): HttpRequest = {
        getRequest(options.courseId)
    }

    private def getRequest(courseId: Option[Int]): HttpRequest = {
        val uri: String = List(
            Some(options.hostServer.baseAddress),
            Some(APlusConstants.PathCourses),
            courseId match {
                case Some(idNumber: Int) => Some(idNumber.toString())
                case None => None
            }
        ).flatten.mkString(CommonConstants.Slash) + CommonConstants.Slash

        options.hostServer.modifyRequest(Http(uri))
    }

    override def getIdentifierAttributes(): Array[String] = {
        Array(
            APlusConstants.AttributeId,
            APlusConstants.AttributeHostName
        )
    }

    def responseToDocumentArray(response: HttpResponse[String]): Array[BsonDocument] = {
        options.courseId match {
            case Some(_) => {
                // if the response is for one course,
                // it should contain only one JSON object
                HttpUtils.responseToDocumentArrayCaseDocument(response)
            }
            case None => {
                // if the response is for all courses,
                // the actual data should be in given as a list of JSON objects under the attribute "results"
                HttpUtils.responseToDocumentArrayCaseAttributeDocument(response, APlusConstants.AttributeResults)
            }
        }
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        // try to always get the detailed course information for each course
        val detailedDocument: BsonDocument = options.courseId match {
            case Some(_) => document
            case None => document.getIntOption(AttributeConstants.AttributeId) match {
                case Some(courseId: Int) => {
                    HttpUtils.getRequestDocument(
                        getRequest(Some(courseId)),
                        HttpConstants.StatusCodeOk
                    ) match {
                        case Some(courseDocument: BsonDocument) =>
                            courseDocument.getIntOption(AttributeConstants.AttributeId) match {
                                case Some(_) => courseDocument
                                case None => document
                            }
                        case None => document
                    }
                }
                case None => document
            }
        }

        addIdentifierAttributes(detailedDocument).append(
            AttributeConstants.AttributeMetadata, getMetadata()
        )
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
                )
            ).asJava
        )
    }
}
