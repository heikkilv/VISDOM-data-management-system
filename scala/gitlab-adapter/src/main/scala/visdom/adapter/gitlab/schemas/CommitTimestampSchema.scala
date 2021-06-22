package visdom.adapter.gitlab.schemas


final case class CommitTimestampSchema(
    project_name: String,
    id: String,
    committed_date: String
)

final case class CommitTimestampSchemaKey(
    project_name: String,
    id: String
)
