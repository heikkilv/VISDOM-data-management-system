package visdom.http.server.services

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import visdom.http.server.services.base.ArtifactServiceBase


class DatasetArtifactService(actorRef: ActorRef)(implicit executionContext: ExecutionContext)
extends ArtifactServiceBase(actorRef)(executionContext)
with MultiInputOptionsDataset
