package visdom.fetchers.aplus

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConstants
import visdom.fetchers.FetcherUtils
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.json.JsonUtils.toBsonArray
import visdom.json.JsonUtils.toBsonValue
import visdom.utils.APlusUtils
import visdom.utils.AttributeConstants
import visdom.utils.CheckQuestionUtils
import visdom.utils.CheckQuestionUtils.EnrichedBsonDocumentWithGdpr
import visdom.utils.CommonConstants
import visdom.utils.metadata.APlusMetadata


class ExerciseFetcher(options: APlusExerciseOptions)
    extends APlusDataHandler(options) {

    private val checkedUsers: Set[Int] = options.gdprOptions match {
        case Some(gdprOptions: GdprOptions) =>
            CheckQuestionUtils.getCheckedUsers(options.courseId, gdprOptions)
        case None => Set.empty
    }

    def getFetcherType(): String = APlusConstants.FetcherTypeExercises
    def getCollectionName(): String = MongoConstants.CollectionExercises
    def usePagination(): Boolean = false

    override def getOptionsDocument(): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourseId -> options.courseId,
            APlusConstants.AttributeUseAnonymization -> options.useAnonymization,
            APlusConstants.AttributeParseNames -> options.parseNames,
            APlusConstants.AttributeIncludeSubmissions -> options.includeSubmissions,
            APlusConstants.AttributeIncludeGitlabData -> options.includeGitlabData
        )
        .appendGdprOptions(options.gdprOptions)
        .appendOption(
            APlusConstants.AttributeModuleId,
            options.moduleId.map(idValue => toBsonValue(idValue))
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
                options.moduleId match {
                    case Some(moduleId: Int) => moduleId.toString()
                    case None => APlusConstants.ErrorForExerciseId
                }
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
        val exerciseId: Option[Int] = detailedDocument.getIntOption(AttributeConstants.Id)

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
            .append(AttributeConstants.Metadata, getMetadata(exerciseId))
            .append(AttributeConstants.Links, getLinkData(submissionIds))
    }

    private def getDetailedDocument(document: BsonDocument): BsonDocument = {
        options.exerciseId match {
            case Some(_) => document
            case None => document.getIntOption(AttributeConstants.Id) match {
                case Some(exerciseId: Int) => {
                    HttpUtils.getRequestDocument(
                        getRequest(Some(exerciseId)),
                        HttpConstants.StatusCodeOk
                    ) match {
                        case Some(exerciseDocument: BsonDocument) =>
                            exerciseDocument.getIntOption(AttributeConstants.Id) match {
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
            .append(APlusConstants.AttributeHostName, toBsonValue(options.hostServer.hostName))
    }

    private def getMetadata(exerciseIdOption: Option[Int]): BsonDocument = {
        val gitLocation: Option[BsonValue] = exerciseIdOption match {
            case Some(exerciseId: Int) =>
                APlusMetadata.exerciseGitLocation
                    .get((options.courseId, exerciseId))
                    .map(location => location.toBsonValue())
            case None => None
        }

        getMetadataBase()
            .append(APlusConstants.AttributeParseNames, toBsonValue(options.parseNames))
            .append(APlusConstants.AttributeIncludeSubmissions, toBsonValue(options.includeSubmissions))
            .append(APlusConstants.AttributeIncludeGitlabData, toBsonValue(options.includeGitlabData))
            .append(APlusConstants.AttributeUseAnonymization, toBsonValue(options.useAnonymization))
            .appendOption(APlusConstants.AttributeOther, gitLocation)
            .appendGdprOptions(options.gdprOptions)
    }

    private def getLinkData(submissionIds: Seq[Int]): BsonDocument = {
        BsonDocument(
            APlusConstants.AttributeCourses -> options.courseId
        )
        .appendOption(
            APlusConstants.AttributeModules, options.moduleId match {
                case Some(moduleId: Int) => Some(toBsonValue(moduleId))
                case None => None
            }
        )
        .appendOption(
            APlusConstants.AttributeSubmissions,
            submissionIds.nonEmpty match {
                case true => Some(toBsonArray(submissionIds))
                case false => None
            }
        )
    }

    def getParsableAttributes(): Seq[Seq[String]] = {
        Seq(
            Seq(APlusConstants.AttributeDisplayName),
            Seq(APlusConstants.AttributeHierarchicalName),
            Seq(APlusConstants.AttributeName)
        )
    }

    private def fetchSubmissions(document: BsonDocument): Seq[Int] = {
        val exerciseIdOption: Option[Int] = document.getIntOption(APlusConstants.AttributeId)
        val submissionIds: Seq[Int] = exerciseIdOption match {
            case Some(exerciseId: Int) => {
                CheckQuestionUtils.getUpdatedGdprOptions(options.gdprOptions, checkedUsers) match {
                    case Some(updatedGdprOptions: GdprOptions) => {
                        val submissionFetcher: SubmissionFetcher = new SubmissionFetcher(
                            APlusSubmissionOptions(
                                hostServer = options.hostServer,
                                mongoDatabase = options.mongoDatabase,
                                courseId = options.courseId,
                                exerciseId = exerciseId,
                                submissionId = None,     // fetch all submissions
                                parseGitAnswers = true,  // always parse the Git answers
                                parseNames = options.parseNames,
                                useAnonymization = options.useAnonymization,
                                gdprOptions = updatedGdprOptions
                            )
                        )

                        val submissionIds: Seq[Int] = FetcherUtils.getFetcherResultIds(submissionFetcher)

                        // get the Git projects list from the submission fetcher
                        val gitProjects = submissionFetcher.getGitProject()
                        // TODO: call GitLab fetcher if the gitlab data is required

                        submissionIds
                    }
                    case None => Seq.empty  // could not create the GdprOptions
                }
            }
            case None => Seq.empty  // no exercise id was set
        }

        exerciseIdOption match {
            case Some(exerciseId: Int) =>
                println(s"Found ${submissionIds.size} submissions to exercise with id ${exerciseId}")
            case None => println("Could not fetch submissions since no exercise id was found")
        }

        submissionIds
    }
}
