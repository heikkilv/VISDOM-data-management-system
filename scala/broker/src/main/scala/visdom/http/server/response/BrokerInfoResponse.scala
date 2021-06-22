package visdom.http.server.response


final case class BrokerInfoResponse(
    componentType: String,
    componentName: String,
    version: String,
    startTime: String,
    apiAddress: String,
    swaggerDefinition: String
) extends ComponentInfoResponse
