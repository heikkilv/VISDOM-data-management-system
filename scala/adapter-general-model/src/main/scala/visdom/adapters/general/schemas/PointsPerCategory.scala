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
