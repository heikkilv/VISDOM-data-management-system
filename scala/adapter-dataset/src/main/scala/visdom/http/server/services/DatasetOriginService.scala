package visdom.http.server.services

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import visdom.http.server.services.base.OriginServiceBase


class DatasetOriginService(actorRef: ActorRef)(implicit executionContext: ExecutionContext)
extends OriginServiceBase(actorRef)(executionContext)
with MultiInputOptionsDataset
