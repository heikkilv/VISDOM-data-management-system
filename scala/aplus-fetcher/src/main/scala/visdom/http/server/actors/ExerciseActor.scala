package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import java.time.ZonedDateTime
import org.mongodb.scala.bson.collection.immutable.Document
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import visdom.http.server.CommonHelpers
import visdom.http.server.ResponseUtils
import visdom.http.server.fetcher.aplus.ExerciseDataQueryOptions
import visdom.http.server.response.StatusResponse
import visdom.http.server.services.constants.APlusFetcherDescriptions
import visdom.fetchers.aplus.APlusExerciseOptions
import visdom.fetchers.aplus.FetcherValues
import visdom.fetchers.aplus.ExerciseFetcher
import visdom.fetchers.aplus.ExerciseSpecificFetchParameters
import visdom.fetchers.aplus.GdprOptions
import visdom.http.server.ServerConstants
import visdom.utils.WartRemoverConstants


class ExerciseActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: ExerciseDataQueryOptions => {
            log.info(s"Received modules query with options: ${queryOptions.toString()}")
            val response: StatusResponse = ExerciseActor.getFetchOptions(queryOptions) match {
                case Right(fetchParameters: ExerciseSpecificFetchParameters) => {
                    // start the course data fetching
                    val courseFetching = Future(ExerciseActor.startCourseFetching(fetchParameters))

                    ResponseUtils.getAcceptedResponse(
                        APlusFetcherDescriptions.StatusAcceptedDescription,
                        queryOptions.toJsObject()
                    )
                }
                case Left(errorDescription: String) => ResponseUtils.getInvalidResponse(errorDescription)
            }
            sender() ! response
        }
    }
}

object ExerciseActor {
    def getFetchOptions(queryOptions: ExerciseDataQueryOptions): Either[String, ExerciseSpecificFetchParameters] = {
        if (!CommonHelpers.isCourseId(Some(queryOptions.courseId))) {
            Left(s"'${queryOptions.courseId}' is not a valid course id")
        }
        else if (!CommonHelpers.isModuleId(Some(queryOptions.moduleId))) {
            Left(s"'${queryOptions.moduleId}' is not a valid module id")
        }
        else if (!CommonHelpers.isExerciseId(queryOptions.exerciseId)) {
            Left(s"'${queryOptions.moduleId}' is not a valid exercise id")
        }
        else if (!ServerConstants.BooleanStrings.contains(queryOptions.parseNames)) {
            Left(s"'${queryOptions.parseNames}' is not a valid value for parseNames")
        }
        else if (!CommonHelpers.areGdprOptions(
            queryOptions.gdprExerciseId,
            queryOptions.gdprFieldName
        )) {
            Left(
                s"'${queryOptions.gdprExerciseId}', '${queryOptions.gdprFieldName}' " +
                s"and '${queryOptions.gdprAcceptedAnswer}' are not a valid values for the GDPR parameters"
            )
        }
        else {
            Right(ExerciseSpecificFetchParameters(
                courseId = queryOptions.courseId.toInt,
                moduleId = queryOptions.moduleId.toInt,
                exerciseId = queryOptions.exerciseId match {
                    case Some(exerciseIdString: String) => Some(exerciseIdString.toInt)
                    case None => None
                },
                parseNames = queryOptions.parseNames.toBoolean,
                includeSubmissions = true,
                gdprOptions = GdprOptions(
                    exerciseId = queryOptions.gdprExerciseId.toInt,
                    fieldName = queryOptions.gdprFieldName,
                    acceptedAnswer = queryOptions.gdprAcceptedAnswer
                )
            ))
        }
    }

    def startCourseFetching(fetchParameters: ExerciseSpecificFetchParameters): Unit = {
        val exerciseFetcherOptions: APlusExerciseOptions = APlusExerciseOptions(
            hostServer = FetcherValues.targetServer,
            mongoDatabase = Some(FetcherValues.targetDatabase),
            courseId = fetchParameters.courseId,
            moduleId = fetchParameters.moduleId,
            exerciseId = fetchParameters.exerciseId,
            parseNames = fetchParameters.parseNames,
            includeSubmissions = fetchParameters.includeSubmissions,
            gdprOptions = fetchParameters.gdprOptions
        )
        val exerciseFetcher = new ExerciseFetcher(exerciseFetcherOptions)
        val exerciseCount = exerciseFetcher.process() match {
            case Some(documents: Array[Document]) => documents.size
            case None => 0
        }
        println(
            s"Found ${exerciseCount} exercises from A+ instance at ${FetcherValues.targetServer.hostName} " +
            s"for course with id ${fetchParameters.courseId} and module with id ${fetchParameters.moduleId}"
        )
    }
}
