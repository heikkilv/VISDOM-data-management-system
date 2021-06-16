package visdom.fetchers.gitlab.queries.info

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.ExecutionContext
import visdom.database.mongodb.MongoConnection
import visdom.fetchers.gitlab.GitlabConstants
import visdom.fetchers.gitlab.GitlabFetcher
import visdom.fetchers.gitlab.Routes


class InfoActor extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = ExecutionContext.global

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    def receive: Receive = {
        case BaseInfo => {
            log.info("Received info query")
            val response: InfoResponse = InfoResponse(
                fetcherName = MongoConnection.applicationName,
                fetcherType = GitlabConstants.FetcherType,
                fetcherVersion = GitlabConstants.FetcherVersion,
                gitlabServer = Routes.server.hostName,
                mongoDatabase = Routes.databaseName,
                startTime = GitlabFetcher.startTime
            )
            sender() ! response
        }
    }
}
