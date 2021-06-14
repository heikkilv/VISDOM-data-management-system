package visdom.adapter.gitlab.queries.timestamps

import akka.actor.Actor
import akka.actor.ActorLogging
import java.util.concurrent.TimeoutException
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import spray.json.JsObject
import visdom.adapter.gitlab.Adapter
import visdom.adapter.gitlab.CommitQuery
import visdom.adapter.gitlab.queries.CommonHelpers
import visdom.adapter.gitlab.queries.Constants
import visdom.adapter.gitlab.queries.GitlabResponse
import visdom.adapter.gitlab.queries.GitlabResponseOk
import visdom.adapter.gitlab.queries.GitlabResponseProblem
import java.util.Date
import java.time.ZonedDateTime
import visdom.adapter.gitlab.TimestampQuery


class TimestampActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    def receive: Receive = {
        case queryOptions: TimestampQueryOptionsSimple => {
            log.info(s"Received commit data query with options: ${queryOptions}")

            val response: GitlabResponse = TimestampActor.getCheckedParameters(queryOptions) match {
                case Left(problem: String) => GitlabResponseProblem(Constants.QueryInvalidStatus, problem)
                case Right(checkedParameters: TimestampQueryOptions) => {
                    // get the response for the query using Spark
                    val sparkResponse: GitlabResponse = try {
                        Await.result(
                            Future(
                                GitlabResponseOk(TimestampQuery.getResult(Adapter.sparkSession, checkedParameters))
                            ),
                            Constants.DefaultWaitDuration
                        )
                    } catch  {
                        case error: TimeoutException => GitlabResponseProblem(
                            Constants.QueryErrorStatus,
                            error.getMessage()
                        )
                    }

                    // check if the response from Spark is empty
                    sparkResponse match {
                        case okResponse: GitlabResponseOk => okResponse.data.fields.isEmpty match {
                            case true => GitlabResponseProblem(
                                Constants.QueryNotFoundStatus,
                                Constants.ResponseDefaultNotFound
                            )
                            case false => sparkResponse
                        }
                        case _ => sparkResponse
                    }
                }
            }

            sender() ! response
        }
    }
}

object TimestampActor {
    def getCheckedParameters(queryOptions: TimestampQueryOptionsSimple): Either[String, TimestampQueryOptions] = {
        val checkedParameters = (
            CommonHelpers.getCheckedParameter(queryOptions.filePaths, CommonHelpers.getCheckedFilePaths),
            CommonHelpers.getCheckedParameter(queryOptions.projectName, CommonHelpers.getCheckedProjectName),
            CommonHelpers.getCheckedParameter(queryOptions.startDate, CommonHelpers.getCheckedDateTime),
            CommonHelpers.getCheckedParameter(queryOptions.endDate, CommonHelpers.getCheckedDateTime)
        )
        if (checkedParameters._1.isLeft) {
            Left(checkedParameters._1.left.getOrElse(""))
        }
        else if (checkedParameters._2.isLeft) {
            Left(checkedParameters._2.left.getOrElse(""))
        }
        else if (checkedParameters._3.isLeft) {
            Left(checkedParameters._3.left.getOrElse(""))
        }
        else if (checkedParameters._4.isLeft) {
            Left(checkedParameters._4.left.getOrElse(""))
        }
        else if ({
            val startDate = checkedParameters._3.right.getOrElse(None)
            val endDate = checkedParameters._4.right.getOrElse(None)
            (startDate.isDefined && endDate.isDefined && !CommonHelpers.lessOrEqual(startDate, endDate))
        }) {
            Left("the endDate must be later than the startDate")
        }
        else {
            Right(TimestampQueryOptions(
                checkedParameters._1.right.getOrElse(Array[String]()),
                checkedParameters._2.right.getOrElse(None),
                checkedParameters._3.right.getOrElse(None),
                checkedParameters._4.right.getOrElse(None)
            ))
        }
    }
}
