package visdom.http.server

import spray.json.RootJsonFormat


trait CourseAdapterServerProtocol
extends ServerProtocol {
    implicit lazy val courseAdapterInfoResponseFormat: RootJsonFormat[response.CourseAdapterInfoResponse] =
        jsonFormat7(response.CourseAdapterInfoResponse)
}
