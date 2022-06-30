// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
