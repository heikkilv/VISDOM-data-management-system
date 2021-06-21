package visdom.http.server.swagger

import com.github.swagger.akka.model.Info
import visdom.http.server.services.BrokerInfoService
import visdom.broker.BrokerValues
import visdom.http.server.services.AdaptersService
import visdom.http.server.services.FetchersService


object SwaggerBrokerDocService extends SwaggerDocService {
    override val host = BrokerValues.apiAddress
    override val info: Info = Info(version = BrokerValues.brokerVersion)
    override val apiClasses: Set[Class[_]] = Set(
        classOf[AdaptersService],
        classOf[FetchersService],
        classOf[BrokerInfoService]
    )
}
