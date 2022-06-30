// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.services

import visdom.http.server.options.DatasetMultiOptions
import visdom.http.server.options.OnlyPageInputOptions
import visdom.adapters.options.ObjectTypes


trait MultiInputOptionsDataset extends MultiInputOptionsTrait {
    def getMultiOptions(
        targetType: String,
        page: Option[String],
        pageSize: Option[String],
        objectType: String,
        query: Option[String],
        dataAttributes: Option[String],
        includedLInks: String
    ): DatasetMultiOptions = {
        DatasetMultiOptions(
            pageOptions = OnlyPageInputOptions(
                page = page,
                pageSize = pageSize
            ),
            targetType = targetType,
            objectType = objectType,
            query = query,
            dataAttributes = dataAttributes,
            includedLinks = includedLInks
        )
    }
}
