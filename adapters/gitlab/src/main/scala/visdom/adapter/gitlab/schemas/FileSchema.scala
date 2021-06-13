package visdom.adapter.gitlab.schemas


final case class FileLinksSchema(
    commits: Array[String]
)

final case class FileSchema(
    project_name: String,
    path: String,
    _links: Option[FileLinksSchema]
)

final case class FileCommitSchema(
    project_name: String,
    path: String,
    commits: Array[String]
)

final case class FileDistinctCommitSchema(
    project_name: String,
    commit: String
)
