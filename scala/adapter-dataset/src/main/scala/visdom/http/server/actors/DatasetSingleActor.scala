// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.actors

import visdom.adapters.dataset.AdapterValues
import visdom.adapters.dataset.usecases.DatasetSingleQuery
import visdom.adapters.general.usecases.SingleQuery
import visdom.adapters.queries.IncludesQueryCode
import visdom.utils.QueryUtils


class DatasetSingleActor extends SingleActor {
    override val singleQueryClass: Class[_ <: SingleQuery] = classOf[DatasetSingleQuery]
    override val singleQueryObject: IncludesQueryCode = DatasetSingleQuery
    override val queryUtils: QueryUtils = AdapterValues.queryUtils
}
