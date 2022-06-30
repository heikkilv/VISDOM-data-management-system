// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.queries

import visdom.adapters.options.BaseQueryOptions
import visdom.adapters.results.BaseResultValue


abstract class BaseCacheQuery(queryOptions: BaseQueryOptions) {
    def cacheCheck(): Boolean
    def updateCache(): (Class[_ <: BaseSparkQuery], BaseQueryOptions)
    def getResults(): Option[BaseResultValue]
}

object BaseCacheQuery extends IncludesQueryCode {
    val queryCode: Int = 0
}
