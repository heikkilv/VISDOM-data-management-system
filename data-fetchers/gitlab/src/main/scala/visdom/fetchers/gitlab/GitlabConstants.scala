package visdom.fetchers.gitlab

object GitlabConstants {
    // the base path for the GitLab API
    val PathBase: String = "api/v4"

    // constants for constructing query paths
    val PathCommits: String = "commits"
    val PathProjects: String = "projects"
    val PathRepository: String = "repository"
    val PathTree: String = "tree"

    // constants for query parameters
    val ParamPage: String = "page"
    val ParamPerPage: String = "per_page"
    val ParamRecursive: String = "recursive"
    val ParamRef: String = "ref"
    val ParamWithStats: String = "with_stats"

    // constants for query and response headers
    val HeaderNextPage: String = "x-next-page"
    val HeaderPrivateToken: String = "Private-Token"

    // constants for JSON attributes
    val AttributePathWithNamespace: String = "path_with_namespace"
    val AttributeProjectName: String = "project_name"

    // constants for default values
    val DefaultPerPage: Int = 100
    val DefaultStartPage: Int = 1

    // constants for HTTP status codes
    val StatusCodeOk: Int = 200

    // constants for predefined error messages
    val ErrorJsonArray: String = "Invalid JSON array"
}
