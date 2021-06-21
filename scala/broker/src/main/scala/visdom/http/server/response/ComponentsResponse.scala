package visdom.http.server.response

import spray.json.JsObject


abstract class ComponentsResponse extends BaseResponse {
    def componentName: String
    def version: String
    def apiAddress: String
    def swaggerDefinition: String
    def information: JsObject
}
