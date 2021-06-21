package visdom.http.server.response

import spray.json.JsObject


final case class AdaptersResponse(
    componentName: String,
    adapterType: String,
    version: String,
    apiAddress: String,
    swaggerDefinition: String,
    information: JsObject
) extends ComponentsResponse
