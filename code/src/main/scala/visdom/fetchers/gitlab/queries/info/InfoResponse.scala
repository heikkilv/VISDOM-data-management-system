package visdom.fetchers.gitlab.queries.info


final case class InfoResponse(
    fetcherName: String,
    fetcherType: String,
    fetcherVersion: String,
    gitlabServer: String,
    mongoDatabase: String,
    startTime: String
)
