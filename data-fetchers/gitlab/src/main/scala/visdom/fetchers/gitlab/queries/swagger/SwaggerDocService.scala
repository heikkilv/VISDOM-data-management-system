package visdom.fetchers.gitlab

import akka.http.scaladsl.server.Directives
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import visdom.fetchers.gitlab.queries.commits.CommitService
import visdom.fetchers.gitlab.queries.swagger.SwaggerConstants


object SwaggerDocService extends SwaggerHttpService {
    private val hostServerName: String = sys.env.getOrElse(
        GitlabConstants.EnvironmentHostName,
        GitlabConstants.DefaultHostName
    )
    private val hostServerPort: String = sys.env.getOrElse(
        GitlabConstants.EnvironmentHostPort,
        GitlabConstants.DefaultHostPort
    )

    override val apiClasses: Set[Class[_]] = Set(classOf[CommitService])
    override val host = List(hostServerName, hostServerPort).mkString(":")
    override val info: Info = Info(version = SwaggerConstants.SwaggerJsonVersion)
    override val unwantedDefinitions: Seq[String] = SwaggerConstants.SwaggerJsonUnwantedDefinitions
}
