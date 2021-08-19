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

    def areGdprOptions(exerciseId: String, fieldName: String, acceptedAnswer: String): Boolean = {
        (isExerciseId(Some(exerciseId)) || exerciseId == -1.toString()) && fieldName.size > 0
    }
}
