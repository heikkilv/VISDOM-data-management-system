package visdom.broker

import com.github.swagger.akka.model.Info
import visdom.http.server.services.BrokerInfoService
import visdom.http.server.swagger.SwaggerDocService


object SwaggerBrokerDocService extends SwaggerDocService {
    override val host = BrokerValues.apiAddress
    override val info: Info = Info(version = BrokerValues.brokerVersion)
    override val apiClasses: Set[Class[_]] = Set(
        classOf[BrokerInfoService]
    )
}
