package visdom.http.server.actors

import visdom.broker.Metadata
import visdom.http.server.response.ComponentInfoResponse


class BrokerInfoActor extends InfoActor {
    def getInfoResponse(): ComponentInfoResponse = {
        Metadata.getInfoResponse()
    }
}
