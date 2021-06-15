package visdom.adapter.gitlab.queries.commits

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


class CommitDataActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    def receive: Receive = {
        case queryOptions: CommitDataQueryOptions => {
            log.info(s"Received commit data query with options: ${queryOptions}")

            val optionProblem: Option[String] = CommitDataActor.getQueryOptionsProblem(queryOptions)

            val response: GitlabResponse = optionProblem match {
                case Some(problem: String) => GitlabResponseProblem(Constants.QueryInvalidStatus, problem)
                case None => {
                    // get the response for the query using Spark
                    val sparkResponse: GitlabResponse = try {
                        Await.result(
                            Future(
                                GitlabResponseOk(CommitQuery.getResult(Adapter.sparkSession, queryOptions))
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

object CommitDataActor {
    def getQueryOptionsProblem(queryOptions: CommitDataQueryOptions): Option[String] = {
        if (!CommonHelpers.isProjectName(queryOptions.projectName)) {
            Some(s"'${queryOptions.projectName.getOrElse("")}' is not a valid project name")
        }
        else if (!CommonHelpers.isUserName(queryOptions.userName)) {
            Some(s"'${queryOptions.userName.getOrElse("")}' is not a valid user name")
        }
        else if (!CommonHelpers.isDateOption(queryOptions.startDate)) {
            Some(s"'${queryOptions.startDate.getOrElse("")}' is not a valid date in ISO 8601 format")
        }
        else if (!CommonHelpers.isDateOption(queryOptions.endDate)) {
            Some(s"'${queryOptions.endDate.getOrElse("")}' is not a valid date in ISO 8601 format")
        }
        else if (
            queryOptions.startDate.isDefined &&
            queryOptions.endDate.isDefined &&
            queryOptions.startDate.getOrElse("") > queryOptions.endDate.getOrElse("")
        ) {
            Some("the endDate must be later than the startDate")
        }
        else {
            None
        }
    }
}
