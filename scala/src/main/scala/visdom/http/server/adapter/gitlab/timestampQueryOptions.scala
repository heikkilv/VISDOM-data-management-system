package visdom.http.server.adapter.gitlab

import visdom.http.server.QueryOptionsBase


abstract class TimestampQueryExtraOptions extends QueryOptionsBase {
    def projectName: Option[String]
    def startDate: Option[String]
    def endDate: Option[String]
}

final case class TimestampQueryOptionsChecked(
    filePaths: Array[String],
    projectName: Option[String],
    startDate: Option[String],
    endDate: Option[String]
) extends TimestampQueryExtraOptions

final case class TimestampQueryOptionsInput(
    filePaths: String,
    projectName: Option[String],
    startDate: Option[String],
    endDate: Option[String]
) extends TimestampQueryExtraOptions
