package visdom.adapters.options

import visdom.http.server.services.constants.GeneralAdapterConstants


abstract class TestTargetType {
    val targetType: String
}

final case object TestTargetEvent
extends TestTargetType {
    val targetType: String = GeneralAdapterConstants.DefaultTarget
}

final case object TestTargetOrigin
extends TestTargetType {
    val targetType: String = GeneralAdapterConstants.ValidTargetOrigin
}

final case object TestTargetAuthor
extends TestTargetType {
    val targetType: String = GeneralAdapterConstants.ValidTargetAuthor
}

final case object TestTargetArtifact
extends TestTargetType {
    val targetType: String = GeneralAdapterConstants.ValidTargetArtifact
}
