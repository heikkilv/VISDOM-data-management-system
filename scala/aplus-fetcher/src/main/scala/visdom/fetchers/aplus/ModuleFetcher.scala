package visdom.fetchers.aplus

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConstants
import visdom.fetchers.FetcherUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonArray
import visdom.json.JsonUtils.toBsonValue
import visdom.http.HttpUtils
import visdom.utils.APlusUtils
import visdom.utils.AttributeConstants
import visdom.utils.CheckQuestionUtils
import visdom.utils.CheckQuestionUtils.EnrichedBsonDocumentWithGdpr
import visdom.utils.CommonConstants
import visdom.utils.metadata.APlusMetadata


class ModuleFetcher(options: APlusModuleOptions)
    extends APlusDataHandler(options) {

    private val checkedUsers: Set[Int] = options.gdprOptions match {
        case Some(gdprOptions: GdprOptions) =>
            CheckQuestionUtils.getCheckedUsers(options.courseId, gdprOptions)
        case None => Set.empty
    }

    def getFetcherType(): String = APlusConstants.FetcherTypeModules
    def getCollectionName(): String = MongoConstants.CollectionModules
    def usePagination(): Boolean = !options.moduleId.isDefined

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourseId -> options.courseId,
            APlusConstants.AttributeUseAnonymization -> options.useAnonymization,
            APlusConstants.AttributeParseNames -> options.parseNames,
            APlusConstants.AttributeIncludeExercises -> options.includeExercises,
            APlusConstants.AttributeIncludeSubmissions -> options.includeSubmissions,
            APlusConstants.AttributeIncludeGitlabData -> options.includeGitlabData
        )
        .appendGdprOptions(options.gdprOptions)
        .appendOption(
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
        val moduleId: Option[Int] = document.getIntOption(AttributeConstants.Id)
        val parsedDocument: BsonDocument = options.parseNames match {
            case true => APlusUtils.parseDocument(document, getParsableAttributes())
            case false => document
        }

        val exerciseIds: Seq[Int] = options.includeExercises match {
            case true => fetchExercises(parsedDocument)
            case false => Seq.empty
        }

        addIdentifierAttributes(parsedDocument)
            .append(AttributeConstants.Metadata, getMetadata(moduleId))
            .append(AttributeConstants.Links, getLinkData(exerciseIds))
    }

    private def addIdentifierAttributes(document: BsonDocument): BsonDocument = {
        document
            .append(APlusConstants.AttributeHostName, toBsonValue(options.hostServer.hostName))
            .append(APlusConstants.AttributeCourseId, toBsonValue(options.courseId))
    }

    private def getMetadata(moduleIdOption: Option[Int]): BsonDocument = {
        val otherMetadata: Option[BsonValue] = moduleIdOption match {
            case Some(moduleId: Int) =>
                APlusMetadata.moduleMetadata
                    .get((options.courseId, moduleId))
                    .map(metadata => metadata.toBsonValue())
            case None => None
        }

        getMetadataBase()
            .append(APlusConstants.AttributeParseNames, toBsonValue(options.parseNames))
            .append(APlusConstants.AttributeIncludeExercises, toBsonValue(options.includeExercises))
            .append(APlusConstants.AttributeIncludeSubmissions, toBsonValue(options.includeSubmissions))
            .append(APlusConstants.AttributeIncludeGitlabData, toBsonValue(options.includeGitlabData))
            .append(APlusConstants.AttributeUseAnonymization, toBsonValue(options.useAnonymization))
            .appendOption(APlusConstants.AttributeOther, otherMetadata)
            .appendGdprOptions(options.gdprOptions)
    }

    def getParsableAttributes(): Seq[Seq[String]] = {
        Seq(
            Seq(APlusConstants.AttributeDisplayName),
            Seq(APlusConstants.AttributeExercises, APlusConstants.AttributeDisplayName),
            Seq(APlusConstants.AttributeExercises, APlusConstants.AttributeHierarchicalName)
        )
    }

    private def getLinkData(exerciseIds: Seq[Int]): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourses -> options.courseId
        )
        .appendOption(
            APlusConstants.AttributeExercises,
            exerciseIds.nonEmpty match {
                case true => Some(toBsonArray(exerciseIds))
                case false => None
            }
        )
    }

    private def fetchExercises(document: BsonDocument): Seq[Int] = {
        val moduleIdOption: Option[Int] = document.getIntOption(APlusConstants.AttributeId)
        val exerciseIds: Seq[Int] = moduleIdOption match {
            case Some(moduleId: Int) => {
                val exerciseFetcher: ExerciseFetcher = new ExerciseFetcher(
                    APlusExerciseOptions(
                        hostServer = options.hostServer,
                        mongoDatabase = options.mongoDatabase,
                        courseId = options.courseId,
                        moduleId = Some(moduleId),
                        exerciseId = None,  // fetch all exercises for the module
                        parseNames = options.parseNames,
                        includeSubmissions = options.includeSubmissions,
                        includeGitlabData = options.includeGitlabData,
                        useAnonymization = options.useAnonymization,
                        gdprOptions = CheckQuestionUtils.getUpdatedGdprOptions(options.gdprOptions, checkedUsers)
                    )
                )

                FetcherUtils.getFetcherResultIds(exerciseFetcher)
            }
            case None => Seq.empty  // no module id was set
        }

        moduleIdOption match {
            case Some(moduleId: Int) =>
                println(s"Found ${exerciseIds.size} exercises in module with id ${moduleId}")
            case None => println("Could not fetch exercises since no module id was found")
        }

        exerciseIds
    }
}
