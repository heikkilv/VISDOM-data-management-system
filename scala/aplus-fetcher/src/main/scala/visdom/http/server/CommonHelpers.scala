package visdom.http.server

import visdom.utils.CheckQuestionUtils


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

    def isBooleanString(inputString: String): Boolean = {
        ServerConstants.BooleanStrings.contains(inputString)
    }

    def getNonBooleanParameter(inputParameters: Seq[(String, String)]): Option[(String, String)] = {
        inputParameters.headOption match {
            case Some((name: String, value: String)) => isBooleanString(value) match {
                case true => getNonBooleanParameter(inputParameters.drop(1))
                case false => Some((name, value))
            }
            case None => None
        }
    }

    def areGdprOptions(exerciseId: Option[String], fieldName: String): Boolean = {
        exerciseId match {
            case Some(exerciseIdString: String) =>
                (
                    exerciseIdString == CheckQuestionUtils.ExerciseIdForNoGdpr.toString() ||
                    isExerciseId(exerciseId)
                ) &&
                fieldName.size > 0
            case None => true
        }
    }
}
