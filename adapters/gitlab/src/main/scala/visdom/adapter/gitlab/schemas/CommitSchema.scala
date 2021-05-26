package visdom.adapter.gitlab.schemas


case class CommitSchema(
    project_name: String,
    committer_name: String,
    committed_date: String
)
