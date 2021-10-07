package visdom.utils


object CourseUtils {
    val GradeRatioDefinitions: Map[Int, Double] = Map(
        5 -> 0.8,
        4 -> 0.7,
        3 -> 0.6,
        2 -> 0.5,
        1 -> 0.4
    )

    val MinGrade: Int = 0
    val MaxGrade: Int = GradeRatioDefinitions.keys.fold(MinGrade)((grade1, grade2) => math.max(grade1, grade2))
    val MaxRatio: Double = GradeRatioDefinitions.getOrElse(MaxGrade, 1.0)

    def getPredictedGrade(coursePoints: Int, studentPoints: Int): Int = {
        // somewhat naive prediction for the course grade
        // based on the amount of the maximum points a student has received
        def predictGrade(pointRatio: Double, maxGrade: Int): Int = {
            maxGrade > MinGrade match {
                case true => pointRatio >= GradeRatioDefinitions.getOrElse(maxGrade, MaxRatio) match {
                    case true => maxGrade
                    case false => predictGrade(pointRatio, maxGrade - 1)
                }
                case false => MinGrade
            }
        }

        predictGrade(studentPoints.toDouble / coursePoints, MaxGrade)
    }
}
