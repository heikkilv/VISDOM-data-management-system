package visdom.fetchers.gitlab.queries.commits

case class CommitQueryOptions(
    projectName: String,
    reference: String,
    startDate: Option[String],
    endDate: Option[String],
    filePath: Option[String],
    includeStatistics: String,
    includeFileLinks: String,
    includeReferenceLinks: String
)
