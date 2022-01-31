package visdom.adapters.general.model.base

import visdom.utils.GeneralUtils


abstract class Origin
extends LinkTrait {
    val source: String
    val context: String

    val id: String = GeneralUtils.getUuid(getType, source, context)

    def link: ItemLink = ItemLink(id, getType)
}
