package visdom.fetchers.gitlab.queries.files


final case class FileQueryOptions(
    projectName: String,
    reference: String,
    filePath: Option[String],
    recursive: String,
    includeCommitLinks: String
)
