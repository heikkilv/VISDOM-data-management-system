// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.options

import visdom.adapters.options.TestQueryOptions
import visdom.adapters.options.TestTargetArtifact
import visdom.adapters.options.TestTargetAuthor
import visdom.adapters.options.TestTargetEvent
import visdom.adapters.options.TestTargetOrigin
import visdom.http.server.services.constants.GeneralAdapterConstants


final case class TestOptions(
    pageOptions: OnlyPageInputOptions,
    target: String,
    token: Option[String]
)
extends BaseInputOptions {
    def toQueryOptions(): TestQueryOptions = {
        val queryPageOptions = pageOptions.toOnlyPageOptions()
        TestQueryOptions(
            page = queryPageOptions.page,
            pageSize = queryPageOptions.pageSize,
            target = target match {
                case GeneralAdapterConstants.ValidTargetOrigin => TestTargetOrigin
                case GeneralAdapterConstants.ValidTargetAuthor => TestTargetAuthor
                case GeneralAdapterConstants.ValidTargetArtifact => TestTargetArtifact
                case _ => TestTargetEvent
            },
            token = token
        )
    }
}
