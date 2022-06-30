// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.actors

import visdom.adapters.dataset.Adapter
import visdom.http.server.response.ComponentInfoResponse


class DatasetAdapterInfoActor
extends AdapterInfoActor {
    override def getInfoResponse(): ComponentInfoResponse = {
        Adapter.adapterMetadata.getInfoResponse()
    }
}
