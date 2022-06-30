// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.options

import visdom.http.server.services.constants.GeneralAdapterConstants
import visdom.utils.SnakeCaseConstants


abstract class LinkTypes {
    val linkType: String
    val linkAttributes: Map[String, Boolean]
}

final case object LinksAll
extends LinkTypes {
    val linkType: String = GeneralAdapterConstants.All
    val linkAttributes: Map[String, Boolean] = Map(
        SnakeCaseConstants.RelatedConstructs -> true,
        SnakeCaseConstants.RelatedEvents -> true
    )
}

final case object LinksConstructs
extends LinkTypes {
    val linkType: String = GeneralAdapterConstants.Constructs
    val linkAttributes: Map[String, Boolean] = Map(
        SnakeCaseConstants.RelatedConstructs -> true,
        SnakeCaseConstants.RelatedEvents -> false
    )
}

final case object LinksEvents
extends LinkTypes {
    val linkType: String = GeneralAdapterConstants.Events
    val linkAttributes: Map[String, Boolean] = Map(
        SnakeCaseConstants.RelatedConstructs -> false,
        SnakeCaseConstants.RelatedEvents -> true
    )
}

final case object LinksNone
extends LinkTypes {
    val linkType: String = GeneralAdapterConstants.None
    val linkAttributes: Map[String, Boolean] = Map(
        SnakeCaseConstants.RelatedConstructs -> false,
        SnakeCaseConstants.RelatedEvents -> false
    )
}
