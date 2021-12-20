package visdom.fetchers.gitlab.queries.events

import akka.actor.Actor
import akka.actor.ActorLogging
import java.time.ZonedDateTime
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import visdom.fetchers.gitlab.EventSpecificFetchParameters
import visdom.fetchers.gitlab.GitlabEventOptions
import visdom.fetchers.gitlab.GitlabEventHandler
import visdom.fetchers.gitlab.Routes.fetcherList
import visdom.fetchers.gitlab.Routes.server
import visdom.fetchers.gitlab.Routes.targetDatabase
import visdom.fetchers.gitlab.queries.CommonHelpers
import visdom.fetchers.gitlab.queries.Constants
import visdom.http.server.response.StatusResponse
import visdom.http.server.fetcher.gitlab.EventQueryOptions
import visdom.http.server.ResponseUtils
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils
import visdom.utils.WartRemoverConstants


class EventActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: EventQueryOptions => {
            log.info(s"Received events query with options: ${queryOptions.toString()}")
            val response: StatusResponse = EventActor.getFetchOptions(queryOptions) match {
                case Right(fetchParameters: EventSpecificFetchParameters) => {
                    // start the event data fetching
                    val _ = Future(EventActor.startEventFetching(fetchParameters))

                    ResponseUtils.getAcceptedResponse(
                        EventConstants.EventStatusAcceptedDescription,
                        queryOptions.toJsObject()
                    )
                }
                case Left(errorDescription: String) => ResponseUtils.getInvalidResponse(errorDescription)
            }

            sender() ! response
        }
    }
}

object EventActor {
    // scalastyle:off cyclomatic.complexity
    def getFetchOptions(queryOptions: EventQueryOptions): Either[String, EventSpecificFetchParameters] = {
        val dateAfter: Option[ZonedDateTime] = GeneralUtils.toZonedDateTime(queryOptions.dateAfter)
        val dateBefore: Option[ZonedDateTime] = GeneralUtils.toZonedDateTime(queryOptions.dateBefore)
        val actionType: String = queryOptions.actionType.getOrElse(CommonConstants.EmptyString)
        val targetType: String = queryOptions.targetType.getOrElse(CommonConstants.EmptyString)

        if (!CommonHelpers.isUserId(queryOptions.userId)) {
            Left(s"'${queryOptions.userId}' is not a valid user id")
        }
        else if (queryOptions.dateAfter.isDefined && !dateAfter.isDefined) {
            Left(s"'${queryOptions.dateAfter.getOrElse("")}' is not valid date in ISO 8601 format")
        }
        else if (queryOptions.dateBefore.isDefined && !dateBefore.isDefined) {
            Left(s"'${queryOptions.dateBefore.getOrElse("")}' is not valid date in ISO 8601 format")
        }
        else if (dateAfter.isDefined && dateBefore.isDefined && !GeneralUtils.lessOrEqual(dateAfter, dateAfter)) {
            Left("the dateBefore must be later than the dateAfter")
        }
        else if (!Constants.ActionTypes.contains(actionType)) {
            Left(s"'${actionType}' is not valid value for actionType")
        }
        else if (!Constants.TargetTypes.contains(targetType)) {
            Left(s"'${targetType}' is not valid value for targetType")
        }
        else if (!Constants.BooleanStrings.contains(queryOptions.useAnonymization)) {
            Left(s"'${queryOptions.useAnonymization}' is not valid value for useAnonymization")
        }
        else {
            Right(EventSpecificFetchParameters(
                userId = queryOptions.userId,
                actionType = queryOptions.actionType,
                targetType = queryOptions.targetType,
                dateAfter = dateAfter,
                dateBefore = dateBefore,
                useAnonymization = queryOptions.useAnonymization.toBoolean
            ))
        }
    }
    // scalastyle:on cyclomatic.complexity

    def startEventFetching(fetchParameters: EventSpecificFetchParameters): Unit = {
        val eventFetcherOptions: GitlabEventOptions = GitlabEventOptions(
            hostServer = server,
            mongoDatabase = Some(targetDatabase),
            userId = fetchParameters.userId,
            actionType = fetchParameters.actionType,
            targetType = fetchParameters.targetType,
            dateAfter = fetchParameters.dateAfter,
            dateBefore = fetchParameters.dateBefore,
            useAnonymization = fetchParameters.useAnonymization
        )
        fetcherList.addFetcher(new GitlabEventHandler(eventFetcherOptions))
    }
}
