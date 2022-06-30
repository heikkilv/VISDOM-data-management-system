// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.dataset

import visdom.adapters.AdapterApp


object Adapter extends AdapterApp {
    val adapterValues: visdom.adapters.AdapterValues = AdapterValues
    val adapterRoutes: visdom.adapters.AdapterRoutes = AdapterRoutes
    val adapterMetadata: visdom.adapters.Metadata = Metadata

    start()
}
