package visdom.fetchers.gitlab

import java.time.ZonedDateTime
import org.mongodb.scala.MongoDatabase


abstract class GitlabFetchOptions {
    val hostServer: GitlabServer
    val mongoDatabase: Option[MongoDatabase]
}

final case class GitlabCommitOptions(
    hostServer: GitlabServer,
    mongoDatabase: Option[MongoDatabase],
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
    mongoDatabase: Option[MongoDatabase],
    projectName: String,
    reference: String,
    filePath: Option[String],
    useRecursiveSearch: Option[Boolean],
    includeCommitLinks: Option[Boolean]
) extends GitlabFetchOptions

final case class GitlabCommitLinkOptions(
    hostServer: GitlabServer,
    mongoDatabase: Option[MongoDatabase],
    projectName: String,
    commitId: String
) extends GitlabFetchOptions
