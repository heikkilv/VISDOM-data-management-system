package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import java.util.concurrent.TimeoutException
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import visdom.adapters.course.options.CourseDataQueryInput
import visdom.adapters.course.options.CourseDataQueryOptions
import visdom.adapters.course.usecases.CourseDataQuery
import visdom.http.HttpConstants
import visdom.http.server.ResponseUtils
import visdom.http.server.response.BaseResponse
import visdom.http.server.response.JsonResponse
import visdom.http.server.services.constants.CourseAdapterDescriptions
import visdom.utils.WartRemoverConstants


class DataQueryActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: CourseDataQueryInput => {
            log.info(s"Received data query with options: ${queryOptions.toString()}")

            val response: BaseResponse = queryOptions.toCourseDataQueryOptions() match {
                case Some(courseDataQueryOptions: CourseDataQueryOptions) => {
                    val sparkResponse: BaseResponse = try {
                        Await.result(
                            Future(
                                JsonResponse(
                                    (new CourseDataQuery(courseDataQueryOptions)).getResults()
                                )
                            ),
                            HttpConstants.DefaultWaitDuration
                        )
                    } catch  {
                        case error: TimeoutException => ResponseUtils.getErrorResponse(error.getMessage())
                    }

                    // check if the response from Spark is empty
                    sparkResponse match {
                        case okResponse: JsonResponse => okResponse.data.fields.isEmpty match {
                            case true => ResponseUtils.getNotFoundResponse(HttpConstants.ResponseDefaultNotFound)
                            case false => sparkResponse
                        }
                        case _ => sparkResponse
                    }
                }
                case None => ResponseUtils.getInvalidResponse(CourseAdapterDescriptions.DefaultErrorDescription)
            }

            sender() ! response
        }
    }
}
