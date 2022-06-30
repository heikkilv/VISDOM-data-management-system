// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.broker

import akka.actor.Props
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import java.time.Instant
import scala.concurrent.ExecutionContextExecutor
import visdom.constants.ComponentConstants
import visdom.http.server.ServerConstants
import visdom.http.server.actors.BrokerInfoActor
import visdom.http.server.actors.BrokerQueryActor
import visdom.http.server.services.AdaptersService
import visdom.http.server.services.BrokerInfoService
import visdom.http.server.services.FetchersService
import visdom.http.server.swagger.SwaggerBrokerDocService
import visdom.http.server.swagger.SwaggerConstants
import visdom.http.server.swagger.SwaggerRoutes
import visdom.utils.CommonConstants
import visdom.utils.EnvironmentVariables.EnvironmentApplicationName
import visdom.utils.EnvironmentVariables.EnvironmentHostName
import visdom.utils.EnvironmentVariables.EnvironmentHostPort
import visdom.utils.EnvironmentVariables.getEnvironmentVariable


object BrokerValues {
    val startTime: String = Instant.now().toString()

    val componentName: String = getEnvironmentVariable(EnvironmentApplicationName)
    val componentType: String = ComponentConstants.BrokerComponentType

    val hostServerName: String = getEnvironmentVariable(EnvironmentHostName)
    val hostServerPort: String = getEnvironmentVariable(EnvironmentHostPort)
    val apiAddress: String = List(hostServerName, hostServerPort).mkString(CommonConstants.DoubleDot)
    val swaggerDefinition: String = SwaggerConstants.SwaggerLocation

    val brokerVersion: String = "0.2"

    implicit val system: ActorSystem = ActorSystem(ServerConstants.DefaultActorSystem)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val routes = Directives.concat(
        new AdaptersService(system.actorOf(Props[BrokerQueryActor])).route,
        new FetchersService(system.actorOf(Props[BrokerQueryActor])).route,
        new BrokerInfoService(system.actorOf(Props[BrokerInfoActor])).route,
        SwaggerRoutes.getSwaggerRouter(SwaggerBrokerDocService)
    )
}
