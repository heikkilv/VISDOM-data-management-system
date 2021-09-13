package visdom.http.server.actors

import visdom.adapters.course.Metadata
import visdom.http.server.response.ComponentInfoResponse


class CourseAdapterInfoActor extends InfoActor {
    def getInfoResponse(): ComponentInfoResponse = {
        Metadata.getInfoResponse()
    }
}
