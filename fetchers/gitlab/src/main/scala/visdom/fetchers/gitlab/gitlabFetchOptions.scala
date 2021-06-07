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


abstract class FileSpecificFetchOptions {
    val projectName: String
    val reference: String
    val filePath: Option[String]
    val recursive: Boolean
    val includeCommitLinks: Boolean
}

final case class FileSpecificFetchParameters(
    projectName: String,
    reference: String,
    filePath: Option[String],
    recursive: Boolean,
    includeCommitLinks: Boolean
) extends FileSpecificFetchOptions

final case class GitlabFileOptions(
    hostServer: GitlabServer,
    mongoDatabase: Option[MongoDatabase],
    projectName: String,
    reference: String,
    filePath: Option[String],
    recursive: Boolean,
    includeCommitLinks: Boolean
) extends GitlabFetchOptions

final case class GitlabCommitLinkOptions(
    hostServer: GitlabServer,
    mongoDatabase: Option[MongoDatabase],
    projectName: String,
    commitId: String
) extends GitlabFetchOptions

abstract class AllDataSpecificFetchOptions {
    val projectName: String
    val reference: String
    val startDate: Option[ZonedDateTime]
    val endDate: Option[ZonedDateTime]
}

final case class AllDataSpecificFetchParameters(
    projectName: String,
    reference: String,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime]
) extends AllDataSpecificFetchOptions
