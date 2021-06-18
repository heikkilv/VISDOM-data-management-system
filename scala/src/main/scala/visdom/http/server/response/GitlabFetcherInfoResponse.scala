package visdom.http.server.response


final case class GitlabFetcherInfoResponse(
    componentName: String,
    componentType: String,
    fetcherType: String,
    version: String,
    gitlabServer: String,
    mongoDatabase: String,
    startTime: String,
    apiAddress: String,
    swaggerDefinition: String
) extends FetcherInfoResponse
