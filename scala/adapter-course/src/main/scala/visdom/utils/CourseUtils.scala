package visdom.utils

import visdom.adapters.course.structs.PointsPerCategory


object CourseUtils {
    val ExerciseScoreLimits: Map[Int, Double] = Map(
        5 -> 0.9,
        4 -> 0.8,
        3 -> 0.7,
        2 -> 0.6,
        1 -> 0.5
    )

    val ProjectScoreLimits: Map[Int, Double] = Map(
        5 -> 0.700,  // 175 out of 250
        4 -> 0.600,  // 150 out of 250
        3 -> 0.500,  // 125 out of 250
        2 -> 0.300,  //  75 out of 250
        1 -> 0.204   //  51 out of 250
    )

    val GuiRatioRequirements: Map[Int, Double] = Map(
        5 -> 0.5,
        4 -> 0.4,
        3 -> 0.3,
        2 -> 0.0
    )

    val MinScore: Int = 0

    def getMaxScore(scoreMap: Map[Int, Double]): Int = {
        scoreMap.keys.fold(MinScore)((grade1, grade2) => math.max(grade1, grade2))
    }

    def getMaxRatio(scoreMap: Map[Int, Double]): Double = {
        scoreMap.getOrElse(getMaxScore(scoreMap), 1.0)
    }

    val MinGrade: Int = 0
    val MaxGrade: Int = GuiRatioRequirements.keys.fold(MinGrade)((grade1, grade2) => math.max(grade1, grade2))

    def getPredictedGrade(courseMaxPoints: PointsPerCategory, studentPoints: PointsPerCategory): Int = {
        def getScore(scoreMap: Map[Int, Double], pointRatio: Double, maxScore: Int): Int = {
            maxScore > MinScore match {
                case true => pointRatio >= scoreMap.getOrElse(maxScore, getMaxRatio(scoreMap)) match {
                    case true => maxScore
                    case false => getScore(scoreMap, pointRatio, maxScore - 1)
                }
                case false => MinScore
            }
        }

        val nScore: Int = getScore(
            ExerciseScoreLimits,
            studentPoints.categoryN.toDouble / courseMaxPoints.categoryN,
            getMaxScore(ExerciseScoreLimits)
        )
        val ngScore: Int = getScore(
            ExerciseScoreLimits,
            (studentPoints.categoryN + studentPoints.categoryG).toDouble / (courseMaxPoints.categoryN + courseMaxPoints.categoryG),
            getMaxScore(ExerciseScoreLimits)
        )
        val exerciseScore: Int = Math.max(nScore, ngScore)
        val projectScore: Int = getScore(
            ProjectScoreLimits,
            studentPoints.categoryP.toDouble / courseMaxPoints.categoryP,
            getMaxScore(ProjectScoreLimits)
        )
        val studentMaxGrade: Int = getScore(
            GuiRatioRequirements,
            studentPoints.categoryG.toDouble / courseMaxPoints.categoryG,
            getMaxScore(GuiRatioRequirements)
        )

        Math.min(studentMaxGrade, (exerciseScore + projectScore) / 2)
    }
}
