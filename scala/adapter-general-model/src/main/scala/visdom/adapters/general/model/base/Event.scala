// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.base

import java.time.ZonedDateTime


abstract class Event
extends LinkTrait
with OriginTrait
with AuthorTrait
with DataTrait
with RelatedItemsHandler {
    val time: ZonedDateTime
    val duration: Double
    val message: String

    def link: ItemLink = ItemLink(id, getType)
}

object Event {
    final val EventType: String = "event"
}
