package visdom.http.server.services

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import visdom.http.server.services.base.AuthorServiceBase


class DatasetAuthorService(actorRef: ActorRef)(implicit executionContext: ExecutionContext)
extends AuthorServiceBase(actorRef)(executionContext)
with MultiInputOptionsDataset
