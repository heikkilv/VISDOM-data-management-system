// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.fetchers.gitlab.queries.project

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import visdom.fetchers.gitlab.GitlabProjectOptions
import visdom.fetchers.gitlab.GitlabProjectHandler
import visdom.fetchers.gitlab.ProjectSpecificFetchParameters
import visdom.fetchers.gitlab.Routes.fetcherList
import visdom.fetchers.gitlab.Routes.server
import visdom.fetchers.gitlab.Routes.targetDatabase
import visdom.fetchers.gitlab.queries.CommonHelpers
import visdom.fetchers.gitlab.queries.Constants
import visdom.http.server.ResponseUtils
import visdom.http.server.ServerProtocol
import visdom.http.server.fetcher.gitlab.ProjectQueryOptions
import visdom.http.server.response.StatusResponse
import visdom.utils.GeneralUtils
import visdom.utils.WartRemoverConstants


class ProjectActor extends Actor with ActorLogging with ServerProtocol {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: ProjectQueryOptions => {
            log.info(s"Received project query with options: ${queryOptions.toString()}")
            val response: StatusResponse = ProjectActor.getFetchOptions(queryOptions) match {
                case Right(fetchParameters: ProjectSpecificFetchParameters) => {
                    // start the project document fetching
                    val _ = Future(ProjectActor.startProjectFetching(fetchParameters))

                    ResponseUtils.getAcceptedResponse(
                        ProjectConstants.ProjectStatusAcceptedDescription,
                        queryOptions.toJsObject()
                    )
                }
                case Left(errorDescription: String) => ResponseUtils.getInvalidResponse(errorDescription)
            }
            sender() ! response
        }
    }
}

object ProjectActor {
    // scalastyle:off cyclomatic.complexity
    def getFetchOptions(queryOptions: ProjectQueryOptions): Either[String, ProjectSpecificFetchParameters] = {
        val projectIdentifier: Either[String, Either[String, Int]] = queryOptions.projectId match {
            case Some(projectId: String) => queryOptions.projectName match {
                case Some(_) => Left("Both, projectId and projectName, cannot be given at the same time")
                case None => GeneralUtils.isIdNumber(projectId) match {
                    case true => Right(Right(projectId.toInt))
                    case false => Left(s"'${projectId}' is not a valid project id")
                }
            }
            case None => queryOptions.projectName match {
                case Some(projectName: String) => CommonHelpers.isProjectName(projectName) match {
                    case true => Right(Left(projectName))
                    case false => Left(s"'${projectName}' is not a valid project name")
                }
                case None => Left("Either projectId or projectName must be given")
            }
        }

        projectIdentifier match {
            case Left(errorMessage: String) => Left(errorMessage)
            case Right(projectIdentifier: Either[String, Int]) =>
                Constants.BooleanStrings.contains(queryOptions.useAnonymization) match {
                    case true => Right(
                        ProjectSpecificFetchParameters(
                            projectIdentifier = projectIdentifier,
                            useAnonymization = queryOptions.useAnonymization.toBoolean
                        )
                    )
                    case false => Left(s"'${queryOptions.useAnonymization}' is not valid value for useAnonymization")
                }
        }
    }
    // scalastyle:on cyclomatic.complexity

    def startProjectFetching(fetchParameters: ProjectSpecificFetchParameters): Unit = {
        val projectFetcherOptions: GitlabProjectOptions = GitlabProjectOptions(
            hostServer = server,
            mongoDatabase = Some(targetDatabase),
            projectIdentifier = fetchParameters.projectIdentifier,
            useAnonymization = fetchParameters.useAnonymization
        )
        fetcherList.addFetcher(new GitlabProjectHandler(projectFetcherOptions))
    }
}
