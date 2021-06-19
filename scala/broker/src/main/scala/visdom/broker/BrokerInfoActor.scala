package visdom.broker

import visdom.http.server.actors.InfoActor
import visdom.http.server.response.ComponentInfoResponse


class BrokerInfoActor extends InfoActor {
    def getInfoResponse(): ComponentInfoResponse = {
        Metadata.getInfoResponse()
    }
}
