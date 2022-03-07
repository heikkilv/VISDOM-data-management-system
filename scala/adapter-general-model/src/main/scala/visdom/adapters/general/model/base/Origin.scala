package visdom.adapters.general.model.base

import visdom.utils.GeneralUtils


abstract class Origin
extends LinkTrait
with DataTrait {
    val source: String
    val context: String

    val id: String = GeneralUtils.getUuid(getType, source, context)

    def link: ItemLink = ItemLink(id, getType)
}

object Origin {
    final val OriginType: String = "origin"
}
