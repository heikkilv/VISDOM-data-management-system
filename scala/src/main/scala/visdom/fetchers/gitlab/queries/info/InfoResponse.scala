package visdom.fetchers.gitlab.queries.info


final case class InfoResponse(
    componentName: String,
    componentType: String,
    fetcherType: String,
    version: String,
    gitlabServer: String,
    mongoDatabase: String,
    startTime: String,
    apiAddress: String,
    swaggerDefinition: String
)
