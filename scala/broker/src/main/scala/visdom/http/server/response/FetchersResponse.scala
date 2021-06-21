package visdom.http.server.response

import spray.json.JsObject


final case class FetchersResponse(
    componentName: String,
    fetcherType: String,
    version: String,
    apiAddress: String,
    swaggerDefinition: String,
    information: JsObject
) extends ComponentsResponse
