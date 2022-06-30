// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.utils.metadata

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import visdom.fetchers.aplus.APlusConstants
import visdom.utils.CommonConstants


final case class ExerciseGitLocation(
    val path: String,
    val isFolder: Boolean
) {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            APlusConstants.AttributePath -> path,
            APlusConstants.AttributeIsFolder -> isFolder
        )
    }
}

object ExerciseGitLocation {
    def toExerciseGitLocation(gitLocationString: String): ExerciseGitLocation = {
        if (gitLocationString.isEmpty()) {
            ExerciseGitLocation(
                path = gitLocationString,
                isFolder = true
            )
        }
        else if (gitLocationString.endsWith(CommonConstants.Slash)) {
            ExerciseGitLocation(
                path = gitLocationString.substring(0, gitLocationString.size - 1),
                isFolder = true
            )
        }
        else {
            ExerciseGitLocation(
                path = gitLocationString,
                isFolder = false
            )
        }
    }
}
