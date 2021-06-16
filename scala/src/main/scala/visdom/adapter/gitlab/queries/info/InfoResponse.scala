package visdom.adapter.gitlab.queries.info


final case class InfoResponse(
    componentType: String,
    componentName: String,
    adapterType: String,
    version: String,
    startTime: String,
    apiAddress: String,
    swaggerDefinition: String
)
