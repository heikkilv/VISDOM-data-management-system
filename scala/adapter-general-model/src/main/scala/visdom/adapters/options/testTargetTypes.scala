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
