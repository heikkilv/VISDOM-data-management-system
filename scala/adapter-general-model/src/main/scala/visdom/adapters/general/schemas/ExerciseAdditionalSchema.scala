// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas


final case class ExerciseAdditionalSchema(
    difficulty: Option[String],
    points_to_pass: Option[Int],
    start_date: Option[String],
    end_date: Option[String]
)

object ExerciseAdditionalSchema {
    def getEmpty(): ExerciseAdditionalSchema = {
        ExerciseAdditionalSchema(None, None, None, None)
    }
}
