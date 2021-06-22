package visdom.http.server.response

import spray.json.JsArray


final case class JsonArrayResponse(
    array: JsArray
) extends BaseResponse
