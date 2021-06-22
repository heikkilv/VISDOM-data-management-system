package visdom.http.server.response


abstract class ComponentInfoResponse extends BaseResponse {
    def componentName: String
    def componentType: String
    def version: String
    def startTime: String
    def apiAddress: String
    def swaggerDefinition: String
}
