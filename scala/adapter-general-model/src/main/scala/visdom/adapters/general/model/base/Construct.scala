package visdom.adapters.general.model.base


abstract class Construct
extends LinkTrait
with OriginTrait
with DataTrait
with RelatedItemsHandler {
    val name: String
    val description: String

    def link: ItemLink = ItemLink(id, getType)
}
