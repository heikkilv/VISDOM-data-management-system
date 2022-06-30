// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas


final case class PointsPerCategory(
    categoryN: Int,
    categoryG: Int,
    categoryP: Int
)

object PointsPerCategory {
    def getDefaultValues(): PointsPerCategory = {
        PointsPerCategory(0, 0, 0)
    }

    def fromPointsDifficultySchema(points: PointsDifficultySchema): PointsPerCategory = {
        PointsPerCategory(
            categoryN = points.category.getOrElse(0),
            categoryG = points.categoryG.getOrElse(0),
            categoryP = points.categoryP.getOrElse(0)
        )
    }
}
