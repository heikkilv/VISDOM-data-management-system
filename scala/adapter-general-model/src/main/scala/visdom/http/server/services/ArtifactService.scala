package visdom.http.server.services

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import visdom.http.server.services.base.ArtifactServiceBase


class ArtifactService(actorRef: ActorRef)(implicit executionContext: ExecutionContext)
extends ArtifactServiceBase(actorRef)(executionContext)
with MultiInputOptionsBase
