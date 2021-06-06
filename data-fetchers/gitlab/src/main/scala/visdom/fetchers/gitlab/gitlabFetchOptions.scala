package visdom.fetchers.gitlab

import java.time.ZonedDateTime
import org.mongodb.scala.MongoDatabase


abstract class GitlabFetchOptions {
    val hostServer: GitlabServer
    val mongoDatabase: Option[MongoDatabase]
}

abstract class CommitSpecificFetchOptions {
    val projectName: String
    val reference: String
    val startDate: Option[ZonedDateTime]
    val endDate: Option[ZonedDateTime]
    val filePath: Option[String]
    val includeStatistics: Boolean
    val includeFileLinks: Boolean
    val includeReferenceLinks: Boolean
}

final case class CommitSpecificFetchParameters(
    projectName: String,
    reference: String,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    filePath: Option[String],
    includeStatistics: Boolean,
    includeFileLinks: Boolean,
    includeReferenceLinks: Boolean
) extends CommitSpecificFetchOptions

final case class GitlabCommitOptions(
    hostServer: GitlabServer,
    mongoDatabase: Option[MongoDatabase],
    projectName: String,
    reference: String,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    filePath: Option[String],
    includeStatistics: Boolean,
    includeFileLinks: Boolean,
    includeReferenceLinks: Boolean
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
