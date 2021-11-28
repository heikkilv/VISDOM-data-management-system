package visdom.adapters.course.structs

import visdom.adapters.course.schemas.ModulePointDifficultySchema


final case class PointsPerCategory(
    categoryN: Int,
    categoryG: Int,
    categoryP: Int
) {
    def add(other: PointsPerCategory): PointsPerCategory = {
        PointsPerCategory(
            categoryN = categoryN + other.categoryN,
            categoryG = categoryG + other.categoryG,
            categoryP = categoryP + other.categoryP
        )
    }
}

object PointsPerCategory {
    def getDefaultValues(): PointsPerCategory = {
        PointsPerCategory(0, 0, 0)
    }

    def fromModulePointDifficultySchema(points: ModulePointDifficultySchema): PointsPerCategory = {
        PointsPerCategory(
            categoryN = points.category.getOrElse(0),
            categoryG = points.categoryG.getOrElse(0),
            categoryP = points.categoryP.getOrElse(0)
        )
    }
}
