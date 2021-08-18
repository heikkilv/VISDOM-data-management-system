package visdom.http.server


object CommonHelpers {
    def isIdNumber(idNumberOption: Option[String]): Boolean = {
        idNumberOption match {
            case Some(idString: String) => {
                val idNumber: Int =
                    try {
                        idString.toInt
                    }
                    catch {
                        case _: NumberFormatException => -1
                    }
                idNumber > 0
            }
            case None => true
        }
    }

    def isCourseId(courseIdOption: Option[String]): Boolean = {
        isIdNumber(courseIdOption)
    }

    def isModuleId(moduleIdOption: Option[String]): Boolean = {
        isIdNumber(moduleIdOption)
    }

    def isExerciseId(exerciseIdOption: Option[String]): Boolean = {
        isIdNumber(exerciseIdOption)
    }
}
