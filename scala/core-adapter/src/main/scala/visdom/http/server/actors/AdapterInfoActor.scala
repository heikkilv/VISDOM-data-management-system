package visdom.http.server.actors

import visdom.adapters.AdapterApp
import visdom.http.server.response.ComponentInfoResponse


class AdapterInfoActor
extends AdapterActor
with InfoActor {
    def getInfoResponse(): ComponentInfoResponse = {
        AdapterApp.adapterApp.adapterMetadata.getInfoResponse()
    }
}
