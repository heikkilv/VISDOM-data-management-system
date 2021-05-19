package visdom.fetchers.gitlab

object GitlabConstants {
    // the base path for the GitLab API
    val PathBase: String = "api/v4"

    // constants for constructing query paths
    val PathCommits: String = "commits"
    val PathDiff: String = "diff"
    val PathProjects: String = "projects"
    val PathRefs: String = "refs"
    val PathRepository: String = "repository"
    val PathTree: String = "tree"

    // constants for query parameters
    val ParamPage: String = "page"
    val ParamPath: String = "path"
    val ParamPerPage: String = "per_page"
    val ParamRecursive: String = "recursive"
    val ParamRef: String = "ref"
    val ParamSince: String = "since"
    val ParamUntil: String = "until"
    val ParamWithStats: String = "with_stats"

    // constants for query and response headers
    val HeaderNextPage: String = "x-next-page"
    val HeaderPrivateToken: String = "Private-Token"

    // constants for JSON attributes
    val AttributeCommits: String = "commits"
    val AttributeDiff: String = "diff"
    val AttributeFiles: String = "files"
    val AttributeId: String = "id"
    val AttributeLinks: String = "_links"
    val AttributePath: String = "path"
    val AttributePathWithNamespace: String = "path_with_namespace"
    val AttributeProjectName: String = "project_name"
    val AttributeRefs: String = "refs"

    // constants for default values
    val DefaultPerPage: Int = 100
    val DefaultStartPage: Int = 1

    // constants for HTTP status codes
    val StatusCodeOk: Int = 200

    // constants for predefined error messages
    val ErrorJsonArray: String = "Invalid JSON array"
}

abstract class GitlabCommitLinkType

final case object GitlabCommitDiff extends GitlabCommitLinkType
final case object GitlabCommitRefs extends GitlabCommitLinkType
