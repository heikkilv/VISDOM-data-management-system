// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.adapter.gitlab

import visdom.http.server.QueryOptionsBase


final case class CommitDataQueryOptions(
    projectName: Option[String],
    userName: Option[String],
    startDate: Option[String],
    endDate: Option[String]
) extends QueryOptionsBase
