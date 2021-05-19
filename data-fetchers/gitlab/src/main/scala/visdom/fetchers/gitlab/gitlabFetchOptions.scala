package visdom.fetchers.gitlab

import java.time.ZonedDateTime

abstract class GitlabFetchOptions {
    def hostServer: GitlabServer
}

final case class GitlabCommitOptions(
    hostServer: GitlabServer,
    projectName: String,
    reference: String,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    filePath: Option[String],
    includeStatistics: Option[Boolean],
    includeFileLinks: Option[Boolean],
    includeReferenceLinks: Option[Boolean]
) extends GitlabFetchOptions

final case class GitlabFileOptions(
    hostServer: GitlabServer,
    projectName: String,
    reference: String,
    filePath: Option[String],
    useRecursiveSearch: Option[Boolean],
    includeCommitLinks: Option[Boolean]
) extends GitlabFetchOptions

final case class GitlabCommitDiffOptions(
    hostServer: GitlabServer,
    projectName: String,
    commitId: String
) extends GitlabFetchOptions
