package visdom.http.server.adapter.gitlab

import visdom.http.server.QueryOptionsBase


final case class CommitDataQueryOptions(
    projectName: Option[String],
    userName: Option[String],
    startDate: Option[String],
    endDate: Option[String]
) extends QueryOptionsBase
