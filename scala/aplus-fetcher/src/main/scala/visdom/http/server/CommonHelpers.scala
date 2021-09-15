package visdom.http.server

import visdom.utils.CheckQuestionUtils
import visdom.utils.GeneralUtils


object CommonHelpers {

    def isCourseId(courseIdOption: Option[String]): Boolean = {
        GeneralUtils.isIdNumber(courseIdOption)
    }

    def isModuleId(moduleIdOption: Option[String]): Boolean = {
        GeneralUtils.isIdNumber(moduleIdOption)
    }

    def isExerciseId(exerciseIdOption: Option[String]): Boolean = {
        GeneralUtils.isIdNumber(exerciseIdOption)
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
