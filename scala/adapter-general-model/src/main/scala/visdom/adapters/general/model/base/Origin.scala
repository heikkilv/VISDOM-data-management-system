// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
