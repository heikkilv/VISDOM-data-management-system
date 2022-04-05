package visdom.http.server.services

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import visdom.http.server.services.base.EventServiceBase


class EventService(actorRef: ActorRef)(implicit executionContext: ExecutionContext)
extends EventServiceBase(actorRef)(executionContext)
with MultiInputOptionsBase
