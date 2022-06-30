// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general

import visdom.adapters.utils.GeneralQueryUtils


object AdapterValues
extends visdom.adapters.AdapterValues {
    override val AdapterType: String = "GeneralModel"
    override val Version: String = "0.1"

    val generalQueryUtils: GeneralQueryUtils = new GeneralQueryUtils(cacheDatabaseName, supportedDatabases)
}
