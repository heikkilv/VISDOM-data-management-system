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
