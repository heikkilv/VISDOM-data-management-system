package visdom.adapter.gitlab.queries.info

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.ExecutionContext
import visdom.adapter.gitlab.GitlabConstants
import visdom.adapter.gitlab.Adapter
import scala.concurrent.Future
import spray.json.JsObject
import visdom.adapter.gitlab.CommitQuery
import visdom.adapter.gitlab.TimestampQuery


class InfoActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    def receive: Receive = {
        case BaseInfo(queryType: Int) => {
            log.info("Received info query")
            val response: InfoResponse = InfoResponse(
                adapterName = "test-adapter",
                adapterType = GitlabConstants.AdapterType,
                adapterVersion = GitlabConstants.AdapterVersion,
                startTime = Adapter.startTime
            )
            val commitQuery: Future[Unit] = Future(
                queryType match {
                    case _ => {
                        val FilePaths: Array[String] = Array("README.md", ".gitignore", "adapters")
                        val timestampResults: JsObject = TimestampQuery.getResult(Adapter.sparkSession, FilePaths)
                        println(timestampResults.prettyPrint)
                    }
                }
            )
            sender() ! response
        }
    }
}
