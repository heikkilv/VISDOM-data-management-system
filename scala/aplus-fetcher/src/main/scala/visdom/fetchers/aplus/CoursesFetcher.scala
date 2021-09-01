package visdom.fetchers.aplus

import org.mongodb.scala.bson.BsonDocument
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConstants
import visdom.fetchers.FetcherUtils
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonArray
import visdom.json.JsonUtils.toBsonValue
import visdom.utils.APlusUtils
import visdom.utils.AttributeConstants
import visdom.utils.CheckQuestionUtils.EnrichedBsonDocumentWithGdpr
import visdom.utils.CommonConstants


class CoursesFetcher(options: APlusCourseOptions)
    extends APlusDataHandler(options) {

    def getFetcherType(): String = APlusConstants.FetcherTypeCourses
    def getCollectionName(): String = MongoConstants.CollectionCourses
    def usePagination(): Boolean = !options.courseId.isDefined

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeUseAnonymization -> options.useAnonymization,
            APlusConstants.AttributeParseNames -> options.parseNames,
            APlusConstants.AttributeIncludeModules -> options.includeModules,
            APlusConstants.AttributeIncludeExercises -> options.includeExercises,
            APlusConstants.AttributeIncludeSubmissions -> options.includeSubmissions,
            APlusConstants.AttributeIncludePoints -> options.includePoints
        ).appendOption(
            APlusConstants.AttributeCourseId,
            options.courseId.map(idValue => toBsonValue(idValue))
        )
        .appendGdprOptions(options.gdprOptions)
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
            case None => getDetailedDocument(document)
        }

        val moduleIds: Seq[Int] = (options.courseId.isDefined && options.includeModules) match {
            // the module information is only fetched if a specific course is targeted
            case true => fetchModuleData(document)
            case false => Seq.empty
        }

        val userIds: Seq[Int] =
            (options.courseId.isDefined && options.includePoints && options.gdprOptions.isDefined) match {
            // the user points information is only fetched if a specific course is targeted and GDPR options are given
                case true => fetchPointData(document)
                case false => Seq.empty
            }

        addIdentifierAttributes(APlusUtils.parseCourseDocument(detailedDocument))
            .append(AttributeConstants.AttributeMetadata, getMetadata())
            .appendOption(AttributeConstants.AttributeLinks, getLinkData(moduleIds, userIds))
    }

    private def getDetailedDocument(document: BsonDocument): BsonDocument = {
        document.getIntOption(AttributeConstants.AttributeId) match {
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

    private def addIdentifierAttributes(document: BsonDocument): BsonDocument = {
        document
            .append(APlusConstants.AttributeHostName, toBsonValue(options.hostServer.hostName))
    }

    private def getMetadata(): BsonDocument = {
        getMetadataBase()
            .append(APlusConstants.AttributeParseNames, toBsonValue(options.parseNames))
            .append(APlusConstants.AttributeIncludeModules, toBsonValue(options.includeModules))
            .append(APlusConstants.AttributeIncludeExercises, toBsonValue(options.includeExercises))
            .append(APlusConstants.AttributeIncludeSubmissions, toBsonValue(options.includeSubmissions))
            .append(APlusConstants.AttributeIncludePoints, toBsonValue(options.includePoints))
            .append(APlusConstants.AttributeUseAnonymization, toBsonValue(options.useAnonymization))
    }

    private def getLinkData(moduleIds: Seq[Int], userIds: Seq[Int]): Option[BsonDocument] = {
        (moduleIds.nonEmpty || userIds.nonEmpty) match {
            case true => Some(
                BsonDocument()
                    .appendOption(APlusConstants.AttributeModules, Some(toBsonArray(moduleIds)))
                    .appendOption(APlusConstants.AttributePoints, Some(toBsonArray(userIds)))
                )
            case false => None
        }
    }

    private def fetchModuleData(document: BsonDocument): Seq[Int] = {
        val courseIdOption: Option[Int] = document.getIntOption(APlusConstants.AttributeId)
        val moduleIds: Seq[Int] = courseIdOption match {
            case Some(courseId: Int) => {
                val moduleFetcher: ModuleFetcher = new ModuleFetcher(
                    APlusModuleOptions(
                        hostServer = options.hostServer,
                        mongoDatabase = options.mongoDatabase,
                        courseId = courseId,
                        moduleId = None,  // fetch all modules
                        parseNames = options.parseNames,
                        includeExercises = options.includeExercises,
                        includeSubmissions = options.includeSubmissions,
                        useAnonymization = options.useAnonymization,
                        gdprOptions = options.gdprOptions
                    )
                )

                FetcherUtils.getFetcherResultIds(moduleFetcher)
            }
            case None => Seq.empty  // no id was found in the given document
        }

        courseIdOption match {
            case Some(courseId: Int) => println(s"Found ${moduleIds.size} modules to course with id ${courseId}")
            case None => println("Could not fetch modules since no course id was found")
        }

        moduleIds
    }

    private def fetchPointData(document: BsonDocument): Seq[Int] = {
        val courseIdOption: Option[Int] = document.getIntOption(APlusConstants.AttributeId)
        val userIds: Seq[Int] = courseIdOption match {
            case Some(courseId: Int) => options.gdprOptions match {
                case Some(gdprOptions: GdprOptions) => {
                    val pointFetcher: PointFetcher = new PointFetcher(
                        APlusPointOptions(
                            hostServer = options.hostServer,
                            mongoDatabase = options.mongoDatabase,
                            courseId = courseId,
                            userId = None,  // fetch points for all users in the course
                            parseNames = options.parseNames,
                            useAnonymization = options.useAnonymization,
                            gdprOptions = gdprOptions
                        )
                    )

                    FetcherUtils.getFetcherResultIds(pointFetcher)
                }
                case None => Seq.empty  // no GdprOptions were found
            }
            case None => Seq.empty  // no id was found in the given document
        }

        courseIdOption match {
            case Some(courseId: Int) =>
                println(s"Found ${userIds.size} users with points document in the course with id ${courseId}")
            case None => println("Could not fetch modules since no course id was found")
        }

        userIds
    }
}
