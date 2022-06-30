// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
