package visdom.http.server.response

import spray.json.JsObject


final case class JsonResponse(
    data: JsObject
) extends BaseResponse
