package visdom.http.server.actors

import visdom.adapters.dataset.Adapter
import visdom.http.server.response.ComponentInfoResponse


class DatasetAdapterInfoActor
extends AdapterInfoActor {
    override def getInfoResponse(): ComponentInfoResponse = {
        Adapter.adapterMetadata.getInfoResponse()
    }
}
