package visdom.adapter.gitlab.queries.swagger

import akka.http.scaladsl.server.Directives
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import visdom.adapter.gitlab.Adapter
import visdom.adapter.gitlab.queries.commits.CommitDataService
import visdom.adapter.gitlab.queries.info.InfoService
import visdom.adapter.gitlab.queries.timestamps.TimestampService


object SwaggerDocService extends SwaggerHttpService {
    override val apiClasses: Set[Class[_]] = Set(
        classOf[CommitDataService],
        classOf[TimestampService],
        classOf[InfoService]
    )
    override val host = Adapter.ApiAddress
    override val info: Info = Info(version = SwaggerConstants.SwaggerJsonVersion)
    override val unwantedDefinitions: Seq[String] = SwaggerConstants.SwaggerJsonUnwantedDefinitions
}
