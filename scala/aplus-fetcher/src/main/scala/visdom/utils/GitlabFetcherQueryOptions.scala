// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils

import visdom.utils.metadata.ExerciseGitLocation


final case class GitlabFetcherQueryOptions(
    val projectNames: Seq[String],
    val gitLocation: ExerciseGitLocation,
    val reference: String,
    val useAnonymization: Boolean
)
