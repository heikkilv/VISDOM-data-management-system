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
import visdom.fetchers.aplus.APlusModuleOptions
import visdom.fetchers.aplus.FetcherValues
import visdom.fetchers.aplus.GdprOptions
import visdom.fetchers.aplus.ModuleFetcher
import visdom.fetchers.aplus.ModuleSpecificFetchParameters
import visdom.http.server.CommonHelpers
import visdom.http.server.ResponseUtils
import visdom.http.server.fetcher.aplus.ModuleDataQueryOptions
import visdom.http.server.response.StatusResponse
import visdom.http.server.services.constants.APlusFetcherDescriptions
import visdom.http.server.services.constants.APlusServerConstants
import visdom.utils.WartRemoverConstants


class ModuleActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: ModuleDataQueryOptions => {
            log.info(s"Received modules query with options: ${queryOptions.toString()}")
            val response: StatusResponse = ModuleActor.getFetchOptions(queryOptions) match {
                case Right(fetchParameters: ModuleSpecificFetchParameters) => {
                    // start the course data fetching
                    val courseFetching = Future(ModuleActor.startCourseFetching(fetchParameters))

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

object ModuleActor {
    def checkQueryOptions(queryOptions: ModuleDataQueryOptions): Option[String] = {
        val nonBooleanParameter: Option[(String, String)] = CommonHelpers.getNonBooleanParameter(
            Seq(
                (APlusServerConstants.ParseNames, queryOptions.parseNames),
                (APlusServerConstants.IncludeExercises, queryOptions.includeExercises),
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

    def getFetchOptions(queryOptions: ModuleDataQueryOptions): Either[String, ModuleSpecificFetchParameters] = {
        checkQueryOptions(queryOptions) match {
            case Some(errorMessage: String) => Left(errorMessage)
            case None =>
                Right(
                    ModuleSpecificFetchParameters(
                        courseId = queryOptions.courseId.toInt,
                        moduleId = queryOptions.moduleId match {
                            case Some(moduleIdString: String) => Some(moduleIdString.toInt)
                            case None => None
                        },
                        parseNames = queryOptions.parseNames.toBoolean,
                        includeExercises = queryOptions.includeExercises.toBoolean,
                        includeSubmissions = queryOptions.includeSubmissions.toBoolean,
                        includeGitlabData = queryOptions.includeGitlabData.toBoolean,
                        useAnonymization = queryOptions.useAnonymization.toBoolean,
                        gdprOptions = queryOptions.gdprExerciseId match {
                            case Some(gdprExerciseId: String) => Some(
                                GdprOptions(
                                    exerciseId = gdprExerciseId.toInt,
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

    def startCourseFetching(fetchParameters: ModuleSpecificFetchParameters): Unit = {
        val moduleFetcherOptions: APlusModuleOptions = APlusModuleOptions(
            hostServer = FetcherValues.targetServer,
            mongoDatabase = Some(FetcherValues.targetDatabase),
            courseId = fetchParameters.courseId,
            moduleId = fetchParameters.moduleId,
            parseNames = fetchParameters.parseNames,
            includeExercises = fetchParameters.includeExercises,
            includeSubmissions = fetchParameters.includeSubmissions,
            includeGitlabData = fetchParameters.includeGitlabData,
            useAnonymization = fetchParameters.useAnonymization,
            gdprOptions = fetchParameters.gdprOptions
        )
        FetcherValues.fetcherList.addFetcher(new ModuleFetcher(moduleFetcherOptions))
    }
}
