// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.artifacts.states


object FileState {
    // TODO: consider the states more carefully for the file objects
    final val FileExistsString: String = "exists"
    final val FileRemovedString: String = "removed"
}
