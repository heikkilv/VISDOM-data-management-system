package visdom.adapter.gitlab


object GitlabConstants {
    val CollectionCommits: String = "commits"
    val CollectionFiles: String = "files"

    val ColumnProjectName: String = "project_name"
    val ColumnCommits: String = "commits"
    val ColumnCommitterName: String = "committer_name"
    val ColumnCommittedDate: String = "committed_date"
    val ColumnDate: String = "date"
    val ColumnId: String = "id"
    val ColumnLinks: String = "_links"
    val ColumnLinksCommits: String = "_links.commits"
    val ColumnPath: String = "path"

    val UtcTimeZone: String = "UTC"
    val DateStringLength: Int = 10

    val AdapterType: String = "GitLab"
    val AdapterVersion: String = "0.2"
    val HttpInternalPort: Int = 8080

    val EnvironmentApplicationName: String = "APPLICATION_NAME"
    val EnvironmentHostName: String = "HOST_NAME"
    val EnvironmentHostPort: String = "HOST_PORT"

    val DefaultApplicationName: String = "GitLab-adapter"
    val DefaultHostName: String = "localhost"
    val DefaultHostPort: String = "9876"

    val DoubleDot: String = ":"
}
