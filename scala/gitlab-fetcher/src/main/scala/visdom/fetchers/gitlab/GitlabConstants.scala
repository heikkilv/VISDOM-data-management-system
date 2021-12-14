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
    val PathTestReport: String = "test_report"
    val PathTree: String = "tree"

    // constants for query parameters
    val ParamLicense: String = "license"
    val ParamPage: String = "page"
    val ParamPath: String = "path"
    val ParamPerPage: String = "per_page"
    val ParamRecursive: String = "recursive"
    val ParamRef: String = "ref"
    val ParamRefName: String = "ref_name"
    val ParamSince: String = "since"
    val ParamStatistics: String = "statistics"
    val PathTrace: String = "trace"
    val ParamUntil: String = "until"
    val ParamUpdatedAfter: String = "updated_after"
    val ParamUpdatedBefore: String = "updated_before"
    val ParamWithStats: String = "with_stats"

    val DefaultLicenseParam: String = true.toString()
    val DefaultStatisticsParam: String = true.toString()

    // constants for query and response headers
    val HeaderNextPage: String = "x-next-page"
    val HeaderPrivateToken: String = "Private-Token"

    // constants for JSON attributes
    val AttributeApiVersion: String = "api_version"
    val AttributeApplicationName: String = "application_name"
    val AttributeAuthorEmail: String = "author_email"
    val AttributeAuthorName: String = "author_name"
    val AttributeAvatarUrl: String = "avatar_url"
    val AttributeBio: String = "bio"
    val AttributeBioHtml: String = "bio_html"
    val AttributeCommit: String = "commit"
    val AttributeCommits: String = "commits"
    val AttributeCommitterEmail: String = "committer_email"
    val AttributeCommitterName: String = "committer_name"
    val AttributeComponentType: String = "component_type"
    val AttributeContainerRegistryImagePrefix: String = "container_registry_image_prefix"
    val AttributeDatabase: String = "database"
    val AttributeDescription: String = "description"
    val AttributeDetailedStatus: String = "detailed_status"
    val AttributeDetailsPath: String = "details_path"
    val AttributeDiff: String = "diff"
    val AttributeDocumentUpdatedCount: String = "documents_updated_count"
    val AttributeEndDate: String = "end_date"
    val AttributeFetcherType: String = "fetcher_type"
    val AttributeFilePath: String = "file_path"
    val AttributeFiles: String = "files"
    val AttributeFullPath: String = "full_path"
    val AttributeGroupFullPath: String = "group_full_path"
    val AttributeGroupName: String = "group_name"
    val AttributeHostName: String = "host_name"
    val AttributeHttpUrlToRepo: String = "http_url_to_repo"
    val AttributeId: String = "id"
    val AttributeIncludeLinksCommits: String = "include_links_commits"
    val AttributeIncludeLinksFiles: String = "include_links_files"
    val AttributeIncludeJobs: String = "include_jobs"
    val AttributeIncludeJobLogs: String = "include_job_logs"
    val AttributeIncludeLinksRefs: String = "include_links_refs"
    val AttributeIncludeReports: String = "include_reports"
    val AttributeIncludeStatistics: String = "include_statistics"
    val AttributeJobLogIncluded: String = "job_log_fetched"
    val AttributeJobs: String = "jobs"
    val AttributeJobTitle: String = "job_title"
    val AttributeLastModified: String = "last_modified"
    val AttributeLicenseUrl: String = "license_url"
    val AttributeLinkedin: String = "linkedin"
    val AttributeLinks: String = "_links"
    val AttributeLocation: String = "location"
    val AttributeLog: String = "log"
    val AttributeMetadata: String = "_metadata"
    val AttributeName: String = "name"
    val AttributeNamespace: String = "namespace"
    val AttributeNameWithNamespace: String = "name_with_namespace"
    val AttributeOptions: String = "options"
    val AttributeOrganization: String = "organization"
    val AttributePath: String = "path"
    val AttributePathWithNamespace: String = "path_with_namespace"
    val AttributePipeline: String = "pipeline"
    val AttributePipelineId: String = "pipeline_id"
    val AttributeProjectId: String = "project_id"
    val AttributeProjectName: String = "project_name"
    val AttributePublicEmail: String = "public_email"
    val AttributeReadmeUrl: String = "readme_url"
    val AttributeRecursive: String = "recursive"
    val AttributeReference: String = "reference"
    val AttributeRefs: String = "refs"
    val AttributeSharedWithGroups: String = "shared_with_groups"
    val AttributeSkype: String = "skype"
    val AttributeSourceServer: String = "source_server"
    val AttributeSshUrlToRepo: String = "ssh_url_to_repo"
    val AttributeStartDate: String = "start_date"
    val AttributeTimestamp: String = "timestamp"
    val AttributeTwitter: String = "twitter"
    val AttributeType: String = "type"
    val AttributeUseAnonymization: String = "use_anonymization"
    val AttributeUser: String = "user"
    val AttributeUserName: String = "username"
    val AttributeVersion: String = "version"
    val AttributeWebsiteUrl: String = "website_url"
    val AttributeWebUrl: String = "web_url"
    val AttributeWorkInformation: String = "work_information"

    // constants for the different GitLab fetcher types
    val FetcherTypeCommits: String = "commits"
    val FetcherTypeCommitDiff: String = "commit_links_files"
    val FetcherTypeCommitRefs: String = "commit_links_refs"
    val FetcherTypeFiles: String = "files"
    val FetcherTypeJobs: String = "jobs"
    val FetcherTypePipeline: String = "pipeline"
    val FetcherTypePipelines: String = "pipelines"
    val FetcherTypePipelineReport: String = "pipeline_report"
    val FetcherTypeProject: String = "project"

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
