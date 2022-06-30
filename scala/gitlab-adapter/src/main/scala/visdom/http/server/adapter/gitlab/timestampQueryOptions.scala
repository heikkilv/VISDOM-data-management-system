// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
