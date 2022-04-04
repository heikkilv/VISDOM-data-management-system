package visdom.adapters.options


trait AttributeFilterObject[FilterType <: AttributeFilterTrait] {
    def fromString(filterString: String): Option[FilterType]
}
