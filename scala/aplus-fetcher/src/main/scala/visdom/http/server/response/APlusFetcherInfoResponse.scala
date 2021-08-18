package visdom.http.server.response


final case class APlusFetcherInfoResponse(
    componentType: String,
    componentName: String,
    fetcherType: String,
    version: String,
    sourceServer: String,
    mongoDatabase: String,
    startTime: String,
    apiAddress: String,
    swaggerDefinition: String
) extends ComponentInfoResponse
