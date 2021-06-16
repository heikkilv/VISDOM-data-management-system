package visdom.adapter.gitlab.queries.commits


final case class CommitDataQueryOptions(
    projectName: Option[String],
    userName: Option[String],
    startDate: Option[String],
    endDate: Option[String]
)
