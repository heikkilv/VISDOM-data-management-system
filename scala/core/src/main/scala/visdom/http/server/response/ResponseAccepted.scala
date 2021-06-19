package visdom.http.server.response

import spray.json.JsObject


final case class ResponseAccepted(
    status: String,
    description: String,
    options: JsObject
) extends StatusResponse
