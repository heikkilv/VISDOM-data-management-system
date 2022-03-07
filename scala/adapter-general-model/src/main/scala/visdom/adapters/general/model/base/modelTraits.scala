package visdom.adapters.general.model.base


trait LinkTrait {
    val id: String
    def getType: String
}

trait OriginTrait {
    val origin: ItemLink
}

trait AuthorTrait {
    val author: ItemLink
}

trait DataTrait {
    val data: Data
}

trait RelatedItemsTrait {
    def relatedEvents: Seq[LinkTrait]
    def relatedConstructs: Seq[LinkTrait]
}
