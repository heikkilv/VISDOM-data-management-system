package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import java.util.concurrent.TimeoutException
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import visdom.adapters.course.options.CommitQueryInput
import visdom.adapters.course.options.CommitQueryOptions
import visdom.adapters.course.usecases.CommitQuery
import visdom.http.HttpConstants
import visdom.http.server.ResponseUtils
import visdom.http.server.response.BaseResponse
import visdom.http.server.response.JsonResponse
import visdom.http.server.services.constants.CourseAdapterDescriptions
import visdom.utils.WartRemoverConstants


class PointsQueryActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case queryOptions: CommitQueryInput => {
            log.info(s"Received points query with options: ${queryOptions.toString()}")

            val response: BaseResponse = queryOptions.toCommitQueryOptions() match {
                case Some(commitQueryOptions: CommitQueryOptions) => {
                    val sparkResponse: BaseResponse = try {
                        Await.result(
                            Future(
                                JsonResponse(
                                    (new CommitQuery(commitQueryOptions)).getResults()
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
