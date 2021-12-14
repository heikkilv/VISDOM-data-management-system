package visdom.http.server.response


final case class InfoResponse(
    componentType: String,
    componentName: String,
    adapterType: String,
    version: String,
    startTime: String,
    apiAddress: String,
    swaggerDefinition: String
) extends ComponentInfoResponse
