package visdom.adapter.gitlab

import org.apache.spark.sql.SparkSession
import spray.json.JsObject
import visdom.spark.Session

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Terminated
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.concat
import java.time.Instant
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.sys.ShutdownHookThread
import visdom.adapter.gitlab.queries.info.InfoActor
import visdom.adapter.gitlab.queries.info.InfoService1
import visdom.adapter.gitlab.queries.info.InfoService2
import visdom.adapter.gitlab.queries.swagger.SwaggerDocService
import visdom.adapter.gitlab.queries.swagger.SwaggerUiSite


object Adapter extends App with SwaggerUiSite {
    // def main(args: Array[String]) {
        val startTime: String = Instant.now().toString()
        val sparkSession: SparkSession = Session.sparkSession

        implicit val system: ActorSystem = ActorSystem("akka-http-sample")
        val shutDownHookThread: ShutdownHookThread = sys.addShutdownHook({
            val termination: Future[Terminated] = {
                Session.sparkSession.stop()
                system.terminate()
            }
        })
        implicit val executionContext: ExecutionContextExecutor = system.dispatcher

        val routes = concat(
            // new AllDataService(system.actorOf(Props[AllDataActor])).route,
            // new CommitService(system.actorOf(Props[CommitActor])).route,
            // new FileService(system.actorOf(Props[FileActor])).route,
            new InfoService1(system.actorOf(Props[InfoActor])).route,
            new InfoService2(system.actorOf(Props[InfoActor])).route,
            SwaggerDocService.routes,
            swaggerUiSiteRoute
        )

        // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
        val serverBinding: Future[Http.ServerBinding] =
            Http().newServerAt("0.0.0.0", GitlabConstants.HttpInternalPort).bindFlow(routes)
    // }
}
