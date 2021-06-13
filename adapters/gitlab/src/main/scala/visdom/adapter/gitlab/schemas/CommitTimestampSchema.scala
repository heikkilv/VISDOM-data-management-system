package visdom.adapter.gitlab.schemas


final case class CommitTimestampSchema(
    project_name: String,
    id: String,
    created_at: String
)

final case class CommitTimestampSchemaKey(
    project_name: String,
    id: String
)
