package visdom.adapter.gitlab.queries.swagger

import akka.http.scaladsl.server.Directives
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import visdom.adapter.gitlab.queries.commits.CommitDataService
import visdom.adapter.gitlab.queries.info.InfoService1
import visdom.adapter.gitlab.queries.info.InfoService2


object SwaggerDocService extends SwaggerHttpService {
    private val hostServerName: String = "localhost"
    private val hostServerPort: String = "9876"

    override val apiClasses: Set[Class[_]] = Set(
        classOf[CommitDataService],
        classOf[InfoService1],
        classOf[InfoService2]
    )
    override val host = List(hostServerName, hostServerPort).mkString(":")
    override val info: Info = Info(version = SwaggerConstants.SwaggerJsonVersion)
    override val unwantedDefinitions: Seq[String] = SwaggerConstants.SwaggerJsonUnwantedDefinitions
}
