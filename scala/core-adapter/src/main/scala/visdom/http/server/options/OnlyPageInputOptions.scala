// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.options

import visdom.adapters.options.PageOptions
import visdom.adapters.options.QueryWithOnlyPageOptions
import visdom.utils.GeneralUtils


final case class OnlyPageInputOptions(
    page: Option[String],
    pageSize: Option[String]
)
extends BaseInputOptions
with PageInputOptions {
    def toOnlyPageOptions(): QueryWithOnlyPageOptions = {
        val pageIntOption: Option[Int] = page match {
            case Some(pageString: String) =>
                GeneralUtils.toIntWithinInterval(pageString, Some(PageOptions.MinPage), None)
            case None => None
        }
        val pageSizeIntOption: Option[Int] = pageSize match {
            case Some(pageSizeString: String) =>
                GeneralUtils.toIntWithinInterval(
                    pageSizeString,
                    Some(PageOptions.MinPageSize),
                    Some(PageOptions.MaxPageSize)
                )
            case None => None
        }

        QueryWithOnlyPageOptions(
            page = pageIntOption.getOrElse(PageOptions.DefaultPage),
            pageSize = pageSizeIntOption.getOrElse(PageOptions.DefaultPageSize)
        )
    }
}
