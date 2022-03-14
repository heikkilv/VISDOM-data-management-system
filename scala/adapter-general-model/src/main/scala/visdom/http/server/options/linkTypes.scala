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
