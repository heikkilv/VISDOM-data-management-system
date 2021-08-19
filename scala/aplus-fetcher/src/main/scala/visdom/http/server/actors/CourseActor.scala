package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import java.time.ZonedDateTime
import org.mongodb.scala.bson.collection.immutable.Document
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import visdom.http.server.CommonHelpers
import visdom.http.server.ResponseUtils
import visdom.http.server.fetcher.aplus.CourseDataQueryOptions
import visdom.http.server.response.StatusResponse
import visdom.http.server.services.constants.APlusFetcherDescriptions
import visdom.fetchers.aplus.APlusCourseOptions
import visdom.fetchers.aplus.CoursesFetcher
import visdom.fetchers.aplus.CourseSpecificFetchParameters
import visdom.fetchers.aplus.FetcherValues
import visdom.utils.WartRemoverConstants


class CourseActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: CourseDataQueryOptions => {
            log.info(s"Received courses query with options: ${queryOptions.toString()}")
            val response: StatusResponse = CourseActor.getFetchOptions(queryOptions) match {
                case Right(fetchParameters: CourseSpecificFetchParameters) => {
                    // start the course data fetching
                    val courseFetching = Future(CourseActor.startCourseFetching(fetchParameters))

                    ResponseUtils.getAcceptedResponse(
                        APlusFetcherDescriptions.StatusAcceptedDescription,
                        queryOptions.toJsObject()
                    )
                }
                case Left(errorDescription: String) => ResponseUtils.getErrorResponse(errorDescription)
            }
            sender() ! response
        }
    }
}

object CourseActor {
    def getFetchOptions(queryOptions: CourseDataQueryOptions): Either[String, CourseSpecificFetchParameters] = {
        if (!CommonHelpers.isCourseId(queryOptions.courseId)) {
            Left(s"'${queryOptions.courseId}' is not a valid course id")
        }
        else {
            Right(CourseSpecificFetchParameters(
                courseId = queryOptions.courseId match {
                    case Some(courseIdString: String) => Some(courseIdString.toInt)
                    case None => None
                }
            ))
        }
    }

    def startCourseFetching(fetchParameters: CourseSpecificFetchParameters): Unit = {
        val courseFetcherOptions: APlusCourseOptions = APlusCourseOptions(
            hostServer = FetcherValues.targetServer,
            mongoDatabase = Some(FetcherValues.targetDatabase),
            courseId = fetchParameters.courseId
        )
        val courseFetcher = new CoursesFetcher(courseFetcherOptions)
        val courseCount = courseFetcher.process() match {
            case Some(documents: Array[Document]) => documents.size
            case None => 0
        }
        println(s"Found ${courseCount} courses from A+ instance at ${FetcherValues.targetServer.hostName}")
    }
}
