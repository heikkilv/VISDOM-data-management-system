// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
