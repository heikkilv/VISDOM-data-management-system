// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters

import java.time.Instant
import visdom.adapters.results.BaseResultValue

final case class QueryResult(
    data: BaseResultValue,
    timestamp: Instant
)
