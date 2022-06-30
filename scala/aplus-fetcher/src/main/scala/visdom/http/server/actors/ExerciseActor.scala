// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import java.time.ZonedDateTime
import org.mongodb.scala.bson.collection.immutable.Document
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import visdom.fetchers.aplus.APlusExerciseOptions
import visdom.fetchers.aplus.FetcherValues
import visdom.fetchers.aplus.ExerciseFetcher
import visdom.fetchers.aplus.ExerciseSpecificFetchParameters
import visdom.fetchers.aplus.GdprOptions
import visdom.http.server.CommonHelpers
import visdom.http.server.ResponseUtils
import visdom.http.server.fetcher.aplus.ExerciseDataQueryOptions
import visdom.http.server.response.StatusResponse
import visdom.http.server.services.constants.APlusFetcherDescriptions
import visdom.http.server.services.constants.APlusServerConstants
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
    def checkQueryOptions(queryOptions: ExerciseDataQueryOptions): Option[String] = {
        val nonBooleanParameter: Option[(String, String)] = CommonHelpers.getNonBooleanParameter(
            Seq(
                (APlusServerConstants.ParseNames, queryOptions.parseNames),
                (APlusServerConstants.IncludeSubmissions, queryOptions.includeSubmissions),
                (APlusServerConstants.IncludeGitlabData, queryOptions.includeGitlabData),
                (APlusServerConstants.UseAnonymization, queryOptions.useAnonymization)
            )
        )

        if (!CommonHelpers.isCourseId(Some(queryOptions.courseId))) {
            Some(s"'${queryOptions.courseId}' is not a valid course id")
        }
        else if (!CommonHelpers.isModuleId(queryOptions.moduleId)) {
            Some(s"'${queryOptions.moduleId}' is not a valid module id")
        }
        else if (!CommonHelpers.isExerciseId(queryOptions.exerciseId)) {
            Some(s"'${queryOptions.moduleId}' is not a valid exercise id")
        }
        else if (!queryOptions.moduleId.isDefined && !queryOptions.exerciseId.isDefined) {
            Some("Either moduleId or exerciseId must be defined")
        }
        else if (nonBooleanParameter.isDefined) {
            nonBooleanParameter.map({case (name, value) => s"'${value}' is not a valid value for ${name}"})
        }
        else if (!CommonHelpers.areGdprOptions(
            queryOptions.gdprExerciseId,
            queryOptions.gdprFieldName
        )) {
            Some(
                s"'${queryOptions.gdprExerciseId}', '${queryOptions.gdprFieldName}' " +
                s"and '${queryOptions.gdprAcceptedAnswer}' are not a valid values for the GDPR parameters"
            )
        }
        else {
            None
        }
    }

    def getFetchOptions(queryOptions: ExerciseDataQueryOptions): Either[String, ExerciseSpecificFetchParameters] = {
        checkQueryOptions(queryOptions) match {
            case Some(errorMessage: String) => Left(errorMessage)
            case None =>
                Right(
                    ExerciseSpecificFetchParameters(
                        courseId = queryOptions.courseId.toInt,
                        moduleId = queryOptions.moduleId match {
                            case Some(moduleIdString: String) => Some(moduleIdString.toInt)
                            case None => None
                        },
                        exerciseId = queryOptions.exerciseId match {
                            case Some(exerciseIdString: String) => Some(exerciseIdString.toInt)
                            case None => None
                        },
                        parseNames = queryOptions.parseNames.toBoolean,
                        includeSubmissions = queryOptions.includeSubmissions.toBoolean,
                        includeGitlabData = queryOptions.includeGitlabData.toBoolean,
                        useAnonymization = queryOptions.useAnonymization.toBoolean,
                        gdprOptions = queryOptions.gdprExerciseId match {
                            case Some(gdprExerciseIdString: String) => Some(
                                GdprOptions(
                                    exerciseId = gdprExerciseIdString.toInt,
                                    fieldName = queryOptions.gdprFieldName,
                                    acceptedAnswer = queryOptions.gdprAcceptedAnswer,
                                    users = None
                                )
                            )
                            case None => None
                        }
                    )
                )
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
            includeGitlabData = fetchParameters.includeGitlabData,
            useAnonymization = fetchParameters.useAnonymization,
            gdprOptions = fetchParameters.gdprOptions
        )
        FetcherValues.fetcherList.addFetcher(new ExerciseFetcher(exerciseFetcherOptions))
    }
}
