package visdom.fetchers.gitlab

object GitlabConstants {
    // constants for constructing query paths
    val PathProjects: String = "projects"
    val PathRepository: String = "repository"
    val PathCommits: String = "commits"

    // constants for query parameters
    val ParamPage: String = "page"
    val ParamPerPage: String = "per_page"
    val ParamRef: String = "ref"
    val ParamWithStats: String = "with_stats"

    // constants for query and response headers
    val HeaderNextPage: String = "x-next-page"
    val HeaderPrivateToken: String = "Private-Token"

    // constants for JSON attributes
    val AttributeProjectName: String = "project_name"

    // constants for default values
    val DefaultPerPage: Int = 100
}
