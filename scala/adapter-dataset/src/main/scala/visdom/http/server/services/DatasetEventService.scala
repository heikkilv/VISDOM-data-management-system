// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.services

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import visdom.http.server.services.base.EventServiceBase


class DatasetEventService(actorRef: ActorRef)(implicit executionContext: ExecutionContext)
extends EventServiceBase(actorRef)(executionContext)
with MultiInputOptionsDataset
