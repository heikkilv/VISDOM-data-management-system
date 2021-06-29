package visdom.fetchers.gitlab

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration


object GitlabConstants {
    // the environmental variables for the GitLab data source
    val EnvironmentGitlabHost: String = "GITLAB_HOST"
    val EnvironmentGitlabToken: String = "GITLAB_TOKEN"
    val EnvironmentGitlabInsecure: String = "GITLAB_INSECURE_CONNECTION"
    val EnvironmentHostName: String = "HOST_NAME"
    val EnvironmentHostPort: String = "HOST_PORT"

    // the default values for the environment variables
    val DefaultGitlabHost: String = ""
    val DefaultGitlabToken: String = ""
    val DefaultGitlabInsecure: String = "false"
    val DefaultHostName: String = "localhost"
    val DefaultHostPort: String = "8765"

    // the base path for the GitLab API
    val PathBase: String = "api/v4"

    // constants for constructing query paths
    val PathCommits: String = "commits"
    val PathDiff: String = "diff"
    val PathJobs: String = "jobs"
    val PathPipelines: String = "pipelines"
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
    val ParamRefName: String = "ref_name"
    val ParamSince: String = "since"
    val PathTrace: String = "trace"
    val ParamUntil: String = "until"
    val ParamUpdatedAfter: String = "updated_after"
    val ParamUpdatedBefore: String = "updated_before"
    val ParamWithStats: String = "with_stats"

    // constants for query and response headers
    val HeaderNextPage: String = "x-next-page"
    val HeaderPrivateToken: String = "Private-Token"

    // constants for JSON attributes
    val AttributeApiVersion: String = "api_version"
    val AttributeApplicationName: String = "application_name"
    val AttributeCommits: String = "commits"
    val AttributeDatabase: String = "database"
    val AttributeDiff: String = "diff"
    val AttributeDocumentUpdatedCount: String = "documents_updated_count"
    val AttributeEndDate: String = "end_date"
    val AttributeFilePath: String = "file_path"
    val AttributeFiles: String = "files"
    val AttributeGitlabServer: String = "gitlab_server"
    val AttributeHostName: String = "host_name"
    val AttributeId: String = "id"
    val AttributeIncludeLinksCommits: String = "include_links_commits"
    val AttributeIncludeLinksFiles: String = "include_links_files"
    val AttributeIncludeJobs: String = "include_jobs"
    val AttributeIncludeJobLogs: String = "include_job_logs"
    val AttributeIncludeLinksRefs: String = "include_links_refs"
    val AttributeIncludeStatistics: String = "include_statistics"
    val AttributeJobLogIncluded: String = "job_log_fetched"
    val AttributeJobs: String = "jobs"
    val AttributeLastModified: String = "last_modified"
    val AttributeLinks: String = "_links"
    val AttributeLog: String = "log"
    val AttributeMetadata: String = "_metadata"
    val AttributeOptions: String = "options"
    val AttributePath: String = "path"
    val AttributePathWithNamespace: String = "path_with_namespace"
    val AttributeProjectName: String = "project_name"
    val AttributeRecursive: String = "recursive"
    val AttributeReference: String = "reference"
    val AttributeRefs: String = "refs"
    val AttributeStartDate: String = "start_date"
    val AttributeTimestamp: String = "timestamp"
    val AttributeType: String = "type"
    val AttributeComponentType: String = "component_type"
    val AttributeFetcherType: String = "fetcher_type"
    val AttributeVersion: String = "version"

    // constants for the different GitLab fetcher types
    val FetcherTypeCommits: String = "commits"
    val FetcherTypeCommitDiff: String = "commit_links_files"
    val FetcherTypeCommitRefs: String = "commit_links_refs"
    val FetcherTypeFiles: String = "files"
    val FetcherTypeJobs: String = "jobs"
    val FetcherTypePipeline: String = "pipeline"
    val FetcherTypePipelines: String = "pipelines"

    // constants for default values
    val DefaultPerPage: Int = 100
    val DefaultStartPage: Int = 1

    // constants for HTTP status codes
    val StatusCodeOk: Int = 200
    val StatusCodeUnauthorized: Int = 401
    val StatusCodeNotFound: Int = 404
    val StatusCodeUnknown: Int = 0

    // constants for predefined error messages
    val ErrorJsonArray: String = "Invalid JSON array"

    // the GitLab API version
    val GitlabApiVersion: Int = 4

    val ComponentType: String = "fetcher"
    val FetcherType: String = "GitLab"
    val FetcherVersion: String = "0.2"
    val HttpInternalPort: Int = 8080

    // the default wait time for HTTP queries to the GitLab API
    val DefaultWaitDurationSeconds: Int = 10
    val DefaultWaitDuration: Duration = Duration(DefaultWaitDurationSeconds, TimeUnit.SECONDS)
}

abstract class GitlabCommitLinkType

final case object GitlabCommitDiff extends GitlabCommitLinkType
final case object GitlabCommitRefs extends GitlabCommitLinkType
