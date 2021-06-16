package visdom.adapter.gitlab.queries.info


final case class InfoResponse(
    adapterName: String,
    adapterType: String,
    adapterVersion: String,
    startTime: String,
    apiAddress: String
)
