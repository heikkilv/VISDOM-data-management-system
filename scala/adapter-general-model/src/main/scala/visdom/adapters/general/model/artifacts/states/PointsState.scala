// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.artifacts.states


object PointsState {
    // TODO: consider the states more carefully for the points objects
    final val NotStarted: String = "not_started"
    final val Active: String = "active"
    final val Passed: String = "passed"
    final val Finished: String = "finished"
    final val Unknown: String = "unknown"
}
