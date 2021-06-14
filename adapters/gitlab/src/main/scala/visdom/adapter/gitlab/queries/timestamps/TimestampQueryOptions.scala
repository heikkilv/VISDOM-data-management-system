package visdom.adapter.gitlab.queries.timestamps

import java.time.ZonedDateTime


final case class TimestampQueryOptions(
    filePaths: Array[String],
    projectName: Option[String],
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime]
)

final case class TimestampQueryOptionsSimple(
    filePaths: String,
    projectName: Option[String],
    startDate: Option[String],
    endDate: Option[String]
)
