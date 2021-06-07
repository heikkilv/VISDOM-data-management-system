package visdom.fetchers.gitlab.queries.all


final case class AllDataQueryOptions(
    projectName: String,
    reference: String,
    startDate: Option[String],
    endDate: Option[String]
)
