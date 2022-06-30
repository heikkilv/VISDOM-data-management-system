// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.base

import visdom.utils.CommonConstants


abstract class Author
extends Artifact {
    def getType: String = Author.AuthorType
}

object Author {
    final val AuthorType: String = "author"
    final val DefaultDescription: String = CommonConstants.EmptyString
}
