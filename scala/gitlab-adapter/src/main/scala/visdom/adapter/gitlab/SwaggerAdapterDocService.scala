package visdom.adapter.gitlab

import com.github.swagger.akka.model.Info
import visdom.adapter.gitlab.queries.commits.CommitDataService
import visdom.adapter.gitlab.queries.info.InfoService
import visdom.adapter.gitlab.queries.projects.ProjectDataService
import visdom.adapter.gitlab.queries.timestamps.TimestampService
import visdom.http.server.swagger.SwaggerDocService


object SwaggerAdapterDocService extends SwaggerDocService {
    override val host = Adapter.ApiAddress
    override val info: Info = Info(version = GitlabConstants.AdapterVersion)
    override val apiClasses: Set[Class[_]] = Set(
        classOf[CommitDataService],
        classOf[TimestampService],
        classOf[ProjectDataService],
        classOf[InfoService]
    )
}
