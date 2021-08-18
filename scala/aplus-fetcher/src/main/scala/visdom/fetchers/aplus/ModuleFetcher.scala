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
import visdom.http.HttpUtils
import visdom.utils.AttributeConstants
import visdom.utils.CommonConstants
import visdom.utils.APlusUtils


class ModuleFetcher(options: APlusModuleOptions)
    extends APlusDataHandler(options) {

    def getFetcherType(): String = APlusConstants.FetcherTypeModules
    def getCollectionName(): String = MongoConstants.CollectionModules
    def usePagination(): Boolean = !options.moduleId.isDefined

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourseId -> options.courseId,
            APlusConstants.AttributeParseNames -> options.parseNames
        ).appendOption(
            APlusConstants.AttributeModuleId,
            options.moduleId.map(idValue => toBsonValue(idValue))
        )
    }

    def getRequest(): HttpRequest = {
        getRequest(options.moduleId)
    }

    private def getRequest(moduleId: Option[Int]): HttpRequest = {
        val uri: String = List(
            Some(options.hostServer.baseAddress),
            Some(APlusConstants.PathCourses),
            Some(options.courseId.toString()),
            Some(APlusConstants.PathExercises),
            moduleId match {
                case Some(idNumber: Int) => Some(idNumber.toString())
                case None => None
            }
        ).flatten.mkString(CommonConstants.Slash) + CommonConstants.Slash

        options.hostServer.modifyRequest(Http(uri))
    }

    override def getIdentifierAttributes(): Array[String] = {
        Array(
            APlusConstants.AttributeId,
            APlusConstants.AttributeCourseId,
            APlusConstants.AttributeHostName
        )
    }

    def responseToDocumentArray(response: HttpResponse[String]): Array[BsonDocument] = {
        options.moduleId match {
            case Some(_) => {
                // if the response is for one exercise module,
                // it should contain only one JSON object
                HttpUtils.responseToDocumentArrayCaseDocument(response)
            }
            case None => {
                // if the response is for all exercise modules in a course,
                // the actual data should be in given as a list of JSON objects under the attribute "results"
                HttpUtils.responseToDocumentArrayCaseAttributeDocument(response, APlusConstants.AttributeResults)
            }
        }
    }

    override def processDocument(document: BsonDocument): BsonDocument = {
        val parsedDocument: BsonDocument = options.parseNames match {
            case true => APlusUtils.parseDocument(document, getParsableAttributes())
            case false => document
        }

        if (options.includeExercises) {
            parsedDocument.getIntOption(APlusConstants.AttributeId) match {
                case Some(moduleId: Int) => fetchExerciseData(moduleId)
                case None =>
            }
        }

        addIdentifierAttributes(parsedDocument)
            .append(AttributeConstants.AttributeMetadata, getMetadata())
    }

    private def addIdentifierAttributes(document: BsonDocument): BsonDocument = {
        document
            .append(APlusConstants.AttributeHostName, new BsonString(options.hostServer.hostName))
            .append(APlusConstants.AttributeCourseId, new BsonInt32(options.courseId))
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

    def getParsableAttributes(): Seq[String] = {
        Seq(APlusConstants.AttributeDisplayName)
    }

    private def fetchExerciseData(moduleId: Int): Unit = {
        val _ = new ExerciseFetcher(
            APlusExerciseOptions(
                hostServer = options.hostServer,
                mongoDatabase = options.mongoDatabase,
                courseId = options.courseId,
                moduleId = moduleId,
                exerciseId = None,
                parseNames = options.parseNames
            )
        ).process()
    }
}
