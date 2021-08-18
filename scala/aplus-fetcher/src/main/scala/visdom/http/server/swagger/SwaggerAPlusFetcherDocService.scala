package visdom.http.server.swagger

import com.github.swagger.akka.model.Info
import visdom.fetchers.aplus.FetcherValues
import visdom.http.server.services.APlusInfoService
import visdom.http.server.services.CourseService
import visdom.http.server.services.ModuleService


object SwaggerAPlusFetcherDocService extends SwaggerDocService {
    override val host = FetcherValues.apiAddress
    override val info: Info = Info(version = FetcherValues.FetcherVersion)
    override val apiClasses: Set[Class[_]] = Set(
        classOf[CourseService],
        classOf[ModuleService],
        classOf[APlusInfoService]
    )
}
