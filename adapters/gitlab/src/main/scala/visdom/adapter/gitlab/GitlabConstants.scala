package visdom.adapter.gitlab


object GitlabConstants {
    val CollectionCommits: String = "commits"
    val CollectionFiles: String = "files"

    val ColumnProjectName: String = "project_name"
    val ColumnCommits: String = "commits"
    val ColumnCommitterName: String = "committer_name"
    val ColumnCommittedDate: String = "committed_date"
    val ColumnCreatedAt: String = "created_at"
    val ColumnDate: String = "date"
    val ColumnId: String = "id"
    val ColumnLinks: String = "_links"
    val ColumnLinksCommits: String = "_links.commits"
    val ColumnPath: String = "path"

    val UtcTimeZone: String = "UTC"

    val AdapterType: String = "GitLab"
    val AdapterVersion: String = "0.2"
    val HttpInternalPort: Int = 8080
}
