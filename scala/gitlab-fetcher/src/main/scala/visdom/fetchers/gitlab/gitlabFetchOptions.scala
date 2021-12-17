package visdom.fetchers.gitlab

import java.time.ZonedDateTime
import org.mongodb.scala.MongoDatabase
import visdom.fetchers.FetchOptions
import visdom.http.server.fetcher.gitlab.Projects
import visdom.utils.CommonConstants
import visdom.utils.GeneralUtils


abstract class GitlabFetchOptions
extends FetchOptions {
    val hostServer: GitlabServer
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
    val useAnonymization: Boolean
}

final case class CommitSpecificFetchParameters(
    projectName: String,
    reference: String,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    filePath: Option[String],
    includeStatistics: Boolean,
    includeFileLinks: Boolean,
    includeReferenceLinks: Boolean,
    useAnonymization: Boolean
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
    includeReferenceLinks: Boolean,
    useAnonymization: Boolean
) extends GitlabFetchOptions {
    override def toString(): String = {
        (
            projectName,
            reference,
            startDate,
            endDate,
            filePath,
            includeStatistics,
            includeFileLinks,
            includeReferenceLinks,
            useAnonymization
        ).toString()
    }
}

abstract class FileSpecificFetchOptions {
    val projectName: String
    val reference: String
    val filePath: Option[String]
    val recursive: Boolean
    val includeCommitLinks: Boolean
    val useAnonymization: Boolean
}

final case class FileSpecificFetchParameters(
    projectName: String,
    reference: String,
    filePath: Option[String],
    recursive: Boolean,
    includeCommitLinks: Boolean,
    useAnonymization: Boolean
) extends FileSpecificFetchOptions

final case class GitlabFileOptions(
    hostServer: GitlabServer,
    mongoDatabase: Option[MongoDatabase],
    projectName: String,
    reference: String,
    filePath: Option[String],
    recursive: Boolean,
    includeCommitLinks: Boolean,
    useAnonymization: Boolean
) extends GitlabFetchOptions {
    override def toString(): String = {
        (
            projectName,
            reference,
            filePath,
            recursive,
            includeCommitLinks,
            useAnonymization
        ).toString()
    }
}

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
    val useAnonymization: Boolean
}

final case class AllDataSpecificFetchParameters(
    projectName: String,
    reference: String,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    useAnonymization: Boolean
) extends AllDataSpecificFetchOptions

abstract class MultiSpecificFetchOptions {
    val projectNames: Seq[String]
    val reference: String
    val filePath: Option[String]
    val recursive: Boolean
    val startDate: Option[ZonedDateTime]
    val endDate: Option[ZonedDateTime]
    val useAnonymization: Boolean
    val projects: Projects
}

abstract class MultiSpecificSingleFetchOptions {
    val projectName: String
    val reference: String
    val filePath: Option[String]
    val recursive: Boolean
    val startDate: Option[ZonedDateTime]
    val endDate: Option[ZonedDateTime]
    val useAnonymization: Boolean
}

final case class MultiSpecificFetchParameters(
    projectNames: Seq[String],
    reference: String,
    filePath: Option[String],
    recursive: Boolean,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    useAnonymization: Boolean,
    projects: Projects
) extends MultiSpecificFetchOptions

final case class MultiSpecificSingleFetchParameters(
    projectName: String,
    reference: String,
    filePath: Option[String],
    recursive: Boolean,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    useAnonymization: Boolean
) extends MultiSpecificSingleFetchOptions {
    override def toString(): String = {
        (
            projectName,
            reference,
            filePath.getOrElse(CommonConstants.EmptyString),
            recursive,
            GeneralUtils.zonedDateTimeToString(startDate),
            GeneralUtils.zonedDateTimeToString(endDate),
            useAnonymization
        ).toString()
    }
}

final case class GitlabPipelinesOptions(
    hostServer: GitlabServer,
    mongoDatabase: Option[MongoDatabase],
    projectName: String,
    reference: String,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    includeReports: Boolean,
    includeJobs: Boolean,
    includeJobLogs: Boolean,
    useAnonymization: Boolean
) extends GitlabFetchOptions {
    override def toString(): String = {
        (
            projectName,
            reference,
            startDate,
            endDate,
            includeReports,
            includeJobs,
            includeJobLogs,
            useAnonymization
        ).toString()
    }
}

final case class GitlabPipelineOptions(
    hostServer: GitlabServer,
    mongoDatabase: Option[MongoDatabase],
    projectName: String,
    pipelineId: Int,
    includeJobLogs: Boolean,
    useAnonymization: Boolean
) extends GitlabFetchOptions {
    override def toString(): String = {
        (
            projectName,
            pipelineId,
            includeJobLogs,
            useAnonymization
        ).toString()
    }
}

final case class PipelinesSpecificFetchParameters(
    projectName: String,
    reference: String,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    includeReports: Boolean,
    includeJobs: Boolean,
    includeJobLogs: Boolean,
    useAnonymization: Boolean
)

final case class GitlabPipelineReportOptions(
    hostServer: GitlabServer,
    mongoDatabase: Option[MongoDatabase],
    projectName: String,
    pipelineId: Int,
    useAnonymization: Boolean
) extends GitlabFetchOptions

abstract class ProjectSpecificFetchOptions {
    val projectIdentifier: Either[String, Int]
    val useAnonymization: Boolean
}

final case class ProjectSpecificFetchParameters(
    projectIdentifier: Either[String, Int],
    useAnonymization: Boolean
) extends ProjectSpecificFetchOptions

final case class GitlabProjectOptions(
    hostServer: GitlabServer,
    mongoDatabase: Option[MongoDatabase],
    projectIdentifier: Either[String, Int],
    useAnonymization: Boolean
) extends GitlabFetchOptions {
    override def toString(): String = {
        (
            projectIdentifier match {
                case Left(projectName: String) => projectName
                case Right(projectId: Int) => projectId.toString()
            },
            useAnonymization
        ).toString()
    }
}

abstract class EventSpecificFetchOptions {
    val userId: String
    val actionType: Option[String]
    val targetType: Option[String]
    val startDate: Option[ZonedDateTime]
    val endDate: Option[ZonedDateTime]
    val useAnonymization: Boolean
}

final case class EventSpecificFetchParameters(
    userId: String,
    actionType: Option[String],
    targetType: Option[String],
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    useAnonymization: Boolean
)

final case class GitlabEventOptions(
    hostServer: GitlabServer,
    mongoDatabase: Option[MongoDatabase],
    userId: String,
    actionType: Option[String],
    targetType: Option[String],
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    useAnonymization: Boolean
) extends GitlabFetchOptions {
    override def toString(): String = {
        (
            userId,
            actionType,
            targetType,
            startDate,
            endDate,
            useAnonymization
        ).toString()
    }
}
