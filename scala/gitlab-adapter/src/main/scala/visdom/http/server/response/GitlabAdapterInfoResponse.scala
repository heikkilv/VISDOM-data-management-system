package visdom.http.server.response


final case class GitlabAdapterInfoResponse(
    componentType: String,
    componentName: String,
    adapterType: String,
    version: String,
    startTime: String,
    apiAddress: String,
    swaggerDefinition: String
) extends AdapterInfoResponse
