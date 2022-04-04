package visdom.http.server.services

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import visdom.http.server.services.base.MetadataServiceBase


class MetadataService(actorRef: ActorRef)(implicit executionContext: ExecutionContext)
extends MetadataServiceBase(actorRef)(executionContext)
with MultiInputOptionsBase
