package visdom.http.server.actors

import visdom.adapters.general.Adapter
import visdom.http.server.response.ComponentInfoResponse


class GeneralAdapterInfoActor
extends AdapterInfoActor {
    override def getInfoResponse(): ComponentInfoResponse = {
        Adapter.adapterMetadata.getInfoResponse()
    }
}
