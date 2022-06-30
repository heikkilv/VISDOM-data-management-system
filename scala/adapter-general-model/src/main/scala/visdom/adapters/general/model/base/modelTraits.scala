// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
