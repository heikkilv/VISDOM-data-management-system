package visdom.fetchers.gitlab

import com.github.swagger.akka.model.Info
import visdom.fetchers.gitlab.queries.all.AllDataService
import visdom.fetchers.gitlab.queries.commits.CommitService
import visdom.fetchers.gitlab.queries.files.FileService
import visdom.fetchers.gitlab.queries.info.InfoService
import visdom.fetchers.gitlab.queries.pipelines.PipelinesService
import visdom.http.server.swagger.SwaggerDocService


object SwaggerFetcherDocService extends SwaggerDocService {
    private val hostServerName: String = sys.env.getOrElse(
        GitlabConstants.EnvironmentHostName,
        GitlabConstants.DefaultHostName
    )
    private val hostServerPort: String = sys.env.getOrElse(
        GitlabConstants.EnvironmentHostPort,
        GitlabConstants.DefaultHostPort
    )

    override val host = List(hostServerName, hostServerPort).mkString(":")
    override val info: Info = Info(version = GitlabConstants.FetcherVersion)
    override val apiClasses: Set[Class[_]] = Set(
        classOf[CommitService],
        classOf[FileService],
        classOf[PipelinesService],
        classOf[AllDataService],
        classOf[InfoService]
    )
}
