package visdom.http.server.actors

import visdom.fetchers.aplus.Metadata
import visdom.http.server.response.ComponentInfoResponse


class APlusInfoActor extends InfoActor {
    def getInfoResponse(): ComponentInfoResponse = {
        Metadata.getInfoResponse()
    }
}
