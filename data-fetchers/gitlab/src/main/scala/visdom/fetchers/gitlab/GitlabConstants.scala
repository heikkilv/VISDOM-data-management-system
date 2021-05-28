package visdom.fetchers.gitlab

object GitlabConstants {
    // the environmental variables for the GitLab data source
    val EnvironmentGitlabHost: String = "GITLAB_HOST"
    val EnvironmentGitlabProject: String = "GITLAB_PROJECT"
    val EnvironmentGitlabToken: String = "GITLAB_TOKEN"
    val EnvironmentGitlabReference: String = "GITLAB_REFERENCE"
    val EnvironmentGitlabInsecure: String = "GITLAB_INSECURE_CONNECTION"

    // the default values for the environment variables
    val DefaultGitlabHost: String = ""
    val DefaultGitlabProject: String = ""
    val DefaultGitlabToken: String = ""
    val DefaultGitlabReference: String = "master"
    val DefaultGitlabInsecure: String = "false"

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
    val AttributeApiVersion: String = "api_version"
    val AttributeCommits: String = "commits"
    val AttributeDiff: String = "diff"
    val AttributeFiles: String = "files"
    val AttributeHostName: String = "host_name"
    val AttributeId: String = "id"
    val AttributeIncludeLinksCommits: String = "include_links_commits"
    val AttributeIncludeLinksFiles: String = "include_links_files"
    val AttributeIncludeLinksRefs: String = "include_links_refs"
    val AttributeIncludeStatistics: String = "include_statistics"
    val AttributeLastModified: String = "last_modified"
    val AttributeLinks: String = "_links"
    val AttributeMetadata: String = "_metadata"
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

    // the GitLab API version
    val GitlabApiVersion: Int = 4
}

abstract class GitlabCommitLinkType

final case object GitlabCommitDiff extends GitlabCommitLinkType
final case object GitlabCommitRefs extends GitlabCommitLinkType
