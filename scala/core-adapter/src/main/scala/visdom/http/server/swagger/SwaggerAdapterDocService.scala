package visdom.http.server.swagger

import com.github.swagger.akka.model.Info
import visdom.adapters.AdapterValues


class SwaggerAdapterDocService(
    adapterValues: AdapterValues,
    serviceClasses: Set[Class[_]]
)
extends SwaggerDocService {
    override val host: String = adapterValues.apiAddress
    override val info: Info = Info(version = adapterValues.Version)
    override val apiClasses: Set[Class[_]] = serviceClasses
}
